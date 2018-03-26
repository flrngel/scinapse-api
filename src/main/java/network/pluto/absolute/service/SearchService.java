package network.pluto.absolute.service;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import network.pluto.absolute.dto.AggregationDto;
import network.pluto.bibliotheca.models.mag.FieldsOfStudy;
import network.pluto.bibliotheca.models.mag.Journal;
import network.pluto.bibliotheca.repositories.FieldsOfStudyRepository;
import network.pluto.bibliotheca.repositories.JournalRepository;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.sampler.Sampler;
import org.elasticsearch.search.aggregations.bucket.sampler.SamplerAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.rescore.QueryRescoreMode;
import org.elasticsearch.search.rescore.QueryRescorerBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final RestHighLevelClient restHighLevelClient;
    private final JournalRepository journalRepository;
    private final FieldsOfStudyRepository fieldsOfStudyRepository;

    @Value("${pluto.server.es.index}")
    private String indexName;

    @Value("${pluto.server.es.index.journal}")
    private String journalIndexName;

    private static final String SAMPLE_AGG_NAME = "sample";
    private static final String JOURNAL_AGG_NAME = "journal";
    private static final String FOS_AGG_NAME = "fos";

    public Page<Long> search(QueryBuilder query, List<SortBuilder> sorts, Pageable pageable) {
        Preconditions.checkNotNull(query);
        Preconditions.checkNotNull(sorts);
        Preconditions.checkNotNull(pageable);

        SearchRequest request = new SearchRequest(indexName);

        SearchSourceBuilder builder = SearchSourceBuilder.searchSource();

        // set query
        builder.query(query);

        // do not retrieve source
        builder.fetchSource(false);

        // apply pagination
        builder.from(pageable.getOffset());
        builder.size(pageable.getPageSize());

        // citation count booster for re-scoring
        FieldValueFactorFunctionBuilder citationFunction = ScoreFunctionBuilders.fieldValueFactorFunction("citation_count").modifier(FieldValueFactorFunction.Modifier.LOG1P);
        FunctionScoreQueryBuilder citationBooster = QueryBuilders.functionScoreQuery(citationFunction).maxBoost(10); // limit boosting

        // re-scoring top 1000 documents only
        QueryRescorerBuilder rescorerBuilder = QueryRescorerBuilder.queryRescorer(citationBooster)
                .windowSize(500)
                .setScoreMode(QueryRescoreMode.Multiply);
        builder.addRescorer(rescorerBuilder);

        // set sort
        sorts.forEach(builder::sort);

        request.source(builder);

        try {
            SearchResponse response = restHighLevelClient.search(request);

            List<Long> list = new ArrayList<>();
            for (SearchHit hit : response.getHits()) {
                list.add(Long.valueOf(hit.getId()));
            }
            return new PageImpl<>(list, pageable, response.getHits().getTotalHits());

        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Elasticsearch exception", e);
        }
    }

    public SearchHit findJournal(String journalTitle) {
        MatchQueryBuilder query = QueryBuilders.matchQuery("full_title", journalTitle);

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(query);

        SearchRequest request = new SearchRequest(journalIndexName);
        request.source(builder);

        try {
            SearchResponse response = restHighLevelClient.search(request);
            SearchHits hits = response.getHits();
            if (hits.getTotalHits() != 1) {
                return null;
            }

            return hits.getAt(0);
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch exception", e);
        }
    }

    public AggregationDto aggregate(QueryBuilder query) {
        Preconditions.checkNotNull(query);

        TermsAggregationBuilder journalAgg = AggregationBuilders.terms(JOURNAL_AGG_NAME).field("journal.id").size(10);
        TermsAggregationBuilder fosAgg = AggregationBuilders.terms(FOS_AGG_NAME).field("fos.id").size(10);

        // add aggregations using top 100 results
        SamplerAggregationBuilder sampleAgg = AggregationBuilders.sampler(SAMPLE_AGG_NAME)
                .subAggregation(journalAgg)
                .subAggregation(fosAgg);

        SearchSourceBuilder builder = SearchSourceBuilder.searchSource()
                .query(query) // set query
                .fetchSource(false) // do not retrieve source
                .size(0) // do not fetch data

                // sampling top 100 result only
                .aggregation(sampleAgg);

        try {
            SearchRequest request = new SearchRequest(indexName).source(builder);
            SearchResponse response = restHighLevelClient.search(request);

            // aggregation
            Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();

            // sampler aggregation for top 100 results
            Sampler sample = (Sampler) aggregationMap.get(SAMPLE_AGG_NAME);
            Map<String, Aggregation> samplerMap = sample.getAggregations().getAsMap();

            return convert(samplerMap);
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch exception", e);
        }
    }

    private AggregationDto convert(Map<String, Aggregation> aggregationMap) {
        List<AggregationDto.Journal> journals = getJournals(aggregationMap);
        List<AggregationDto.Fos> fosList = getFosList(aggregationMap);

        AggregationDto dto = new AggregationDto();
        dto.journals = journals;
        dto.fosList = fosList;
        return dto;
    }

    private List<AggregationDto.Journal> getJournals(Map<String, Aggregation> aggregationMap) {
        Terms journal = (Terms) aggregationMap.get(JOURNAL_AGG_NAME);
        List<Long> journalIds = journal.getBuckets()
                .stream()
                .map(j -> (long) j.getKey())
                .collect(Collectors.toList());
        Map<Long, Journal> journalMap = journalRepository.findByIdIn(journalIds).stream()
                .collect(Collectors.toMap(
                        Journal::getId,
                        Function.identity()
                ));
        return journal.getBuckets()
                .stream()
                .map(j -> {
                    long key = (long) j.getKey();
                    Journal one = journalMap.get(key);
                    if (one == null) {
                        return null;
                    }

                    AggregationDto.Journal journalDto = new AggregationDto.Journal();
                    journalDto.id = key;
                    journalDto.title = one.getDisplayName();
//                    journalDto.docCount = j.getDocCount(); // sampled count, cannot use yet.

                    return journalDto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<AggregationDto.Fos> getFosList(Map<String, Aggregation> aggregationMap) {
        Terms fos = (Terms) aggregationMap.get(FOS_AGG_NAME);
        List<Long> fosIds = fos.getBuckets()
                .stream()
                .map(f -> (long) f.getKey())
                .collect(Collectors.toList());
        Map<Long, FieldsOfStudy> fosMap = fieldsOfStudyRepository.findByIdIn(fosIds)
                .stream()
                .collect(Collectors.toMap(
                        FieldsOfStudy::getId,
                        Function.identity()
                ));
        return fos.getBuckets()
                .stream()
                .map(f -> {
                    long key = (long) f.getKey();
                    FieldsOfStudy one = fosMap.get(key);
                    if (one == null) {
                        return null;
                    }

                    AggregationDto.Fos fosDto = new AggregationDto.Fos();
                    fosDto.id = key;
                    fosDto.name = one.getDisplayName();
//                    fosDto.docCount = f.getDocCount(); // sampled count, cannot use yet.

                    return fosDto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
