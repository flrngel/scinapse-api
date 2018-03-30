package network.pluto.absolute.service;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import network.pluto.absolute.dto.AggregationDto;
import network.pluto.absolute.util.Query;
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
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filters.Filters;
import org.elasticsearch.search.aggregations.bucket.filters.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filters.FiltersAggregator;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregator;
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
import java.time.Year;
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
    private static final String YEAR_AGG_NAME = "year";
    private static final String IF_AGG_NAME = "if";
    private static final String IF_10_AGG_NAME = "if10";

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

    public AggregationDto aggregateFromSample(Query query) {
        Preconditions.checkNotNull(query);

        TermsAggregationBuilder journalAgg = AggregationBuilders.terms(JOURNAL_AGG_NAME).field("journal.id").size(10);
        TermsAggregationBuilder fosAgg = AggregationBuilders.terms(FOS_AGG_NAME).field("fos.id").size(10);

        // add aggregations using top 100 results
        SamplerAggregationBuilder sampleAgg = AggregationBuilders.sampler(SAMPLE_AGG_NAME)
                .subAggregation(journalAgg)
                .subAggregation(fosAgg);

        SearchSourceBuilder builder = SearchSourceBuilder.searchSource()
                .query(query.getMainQueryClause()) // set query
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

    // for calculate doc count for each buckets
    public void enhanceAggregation(Query query, AggregationDto dto) {
        double year = (double) Year.now().getValue();
        RangeAggregationBuilder yearAgg = AggregationBuilders.range(YEAR_AGG_NAME)
                .field("year")
                .addRange(year - 10, year + 1)
                .subAggregation(AggregationBuilders.histogram(YEAR_AGG_NAME)
                        .field("year")
                        .interval(1)
                        .minDocCount(0)
                        .extendedBounds(year - 10, year));
        FilterAggregationBuilder yearAggFiltered = AggregationBuilders
                .filter(YEAR_AGG_NAME, query.getFilter().toYearAggFilter())
                .subAggregation(yearAgg);

        RangeAggregationBuilder ifAgg = AggregationBuilders.range(IF_AGG_NAME)
                .field("journal.impact_factor")
                .addRange(0, 10)
                .subAggregation(AggregationBuilders.histogram(IF_AGG_NAME)
                        .field("journal.impact_factor")
                        .interval(1)
                        .minDocCount(0)
                        .extendedBounds(0, 9));
        RangeAggregationBuilder if10Agg = AggregationBuilders.range(IF_10_AGG_NAME)
                .field("journal.impact_factor")
                .addRange(new RangeAggregator.Range(IF_10_AGG_NAME, 10d, null));
        FilterAggregationBuilder ifAggFiltered = AggregationBuilders
                .filter(IF_AGG_NAME, query.getFilter().toImpactFactorAggFilter())
                .subAggregation(ifAgg)
                .subAggregation(if10Agg);

        FiltersAggregator.KeyedFilter[] journalFilters = dto.journals
                .stream()
                .map(j -> {
                    TermQueryBuilder journalTermQuery = QueryBuilders.termQuery("journal.id", j.id);
                    return new FiltersAggregator.KeyedFilter(String.valueOf(j.id), journalTermQuery);
                })
                .toArray(FiltersAggregator.KeyedFilter[]::new);
        FiltersAggregationBuilder journalAgg = AggregationBuilders.filters(JOURNAL_AGG_NAME, journalFilters);
        FilterAggregationBuilder journalAggFiltered = AggregationBuilders
                .filter(JOURNAL_AGG_NAME, query.getFilter().toJournalAggFilter())
                .subAggregation(journalAgg);

        FiltersAggregator.KeyedFilter[] fosFilters = dto.fosList
                .stream()
                .map(f -> {
                    TermQueryBuilder fosTermQuery = QueryBuilders.termQuery("fos.id", f.id);
                    return new FiltersAggregator.KeyedFilter(String.valueOf(f.id), fosTermQuery);
                })
                .toArray(FiltersAggregator.KeyedFilter[]::new);
        FiltersAggregationBuilder fosAgg = AggregationBuilders.filters(FOS_AGG_NAME, fosFilters);
        FilterAggregationBuilder fosAggFiltered = AggregationBuilders
                .filter(FOS_AGG_NAME, query.getFilter().toFosAggFilter())
                .subAggregation(fosAgg);

        SearchSourceBuilder builder = SearchSourceBuilder.searchSource()
                .query(query.getMainQueryClause()) // set query
                .fetchSource(false) // do not retrieve source
                .size(0) // do not fetch data

                // for bucket doc count
                .aggregation(journalAggFiltered)
                .aggregation(fosAggFiltered)
                .aggregation(yearAggFiltered)
                .aggregation(ifAggFiltered);

        try {
            SearchRequest request = new SearchRequest(indexName).source(builder);
            SearchResponse response = restHighLevelClient.search(request);

            // enhance aggregation
            Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();
            enhanceAggregation(dto, aggregationMap);
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch exception", e);
        }
    }

    private void enhanceAggregation(AggregationDto dto, Map<String, Aggregation> aggregationMap) {
        dto.years = getYears(aggregationMap);
        dto.impactFactors = getImpactFactors(aggregationMap);

        Filter journalFiltered = (Filter) aggregationMap.get(JOURNAL_AGG_NAME);
        Filters journal = journalFiltered.getAggregations().get(JOURNAL_AGG_NAME);
        Filter fosFiltered = (Filter) aggregationMap.get(FOS_AGG_NAME);
        Filters fos = fosFiltered.getAggregations().get(FOS_AGG_NAME);

        dto.journals.forEach(j -> {
            Filters.Bucket bucket = journal.getBucketByKey(String.valueOf(j.id));
            if (bucket == null) {
                return;
            }
            j.docCount = bucket.getDocCount();
        });
        dto.fosList.forEach(f -> {
            Filters.Bucket bucket = fos.getBucketByKey(String.valueOf(f.id));
            if (bucket == null) {
                return;
            }
            f.docCount = bucket.getDocCount();
        });
    }

    private AggregationDto convert(Map<String, Aggregation> aggregationMap) {
        List<AggregationDto.Journal> journals = getJournals(aggregationMap);
        List<AggregationDto.Fos> fosList = getFosList(aggregationMap);

        if (journals.isEmpty() && fosList.isEmpty()) {
            return AggregationDto.unavailable();
        }

        AggregationDto dto = AggregationDto.available();
        dto.journals = journals;
        dto.fosList = fosList;
        return dto;
    }

    private List<AggregationDto.Year> getYears(Map<String, Aggregation> aggregationMap) {
        Filter yearFiltered = (Filter) aggregationMap.get(YEAR_AGG_NAME);
        Range year = yearFiltered.getAggregations().get(YEAR_AGG_NAME);
        List<? extends Range.Bucket> buckets = year.getBuckets();
        Histogram yearHistogram = buckets.get(0).getAggregations().get(YEAR_AGG_NAME);

        List<AggregationDto.Year> years = yearHistogram.getBuckets()
                .stream()
                .map(y -> {
                    AggregationDto.Year yearDto = new AggregationDto.Year();
                    yearDto.year = ((Double) y.getKey()).intValue();
                    yearDto.docCount = y.getDocCount();
                    return yearDto;
                })
                .collect(Collectors.toList());

        AggregationDto.Year allYear = new AggregationDto.Year();
        allYear.year = AggregationDto.ALL;
        allYear.docCount = yearFiltered.getDocCount();
        years.add(allYear);

        return years;
    }

    private List<AggregationDto.ImpactFactor> getImpactFactors(Map<String, Aggregation> aggregationMap) {
        Filter ifFiltered = (Filter) aggregationMap.get(IF_AGG_NAME);

        Range impactFactor = ifFiltered.getAggregations().get(IF_AGG_NAME);
        List<? extends Range.Bucket> buckets = impactFactor.getBuckets();
        Histogram ifHistogram = buckets.get(0).getAggregations().get(IF_AGG_NAME);
        List<AggregationDto.ImpactFactor> impactFactors = ifHistogram.getBuckets()
                .stream()
                .map(y -> {
                    AggregationDto.ImpactFactor ifDto = new AggregationDto.ImpactFactor();
                    int from = ((Double) y.getKey()).intValue();
                    ifDto.from = from;
                    ifDto.to = from + 1;
                    ifDto.docCount = y.getDocCount();
                    return ifDto;
                })
                .collect(Collectors.toList());

        Range impactFactor10 = ifFiltered.getAggregations().get(IF_10_AGG_NAME);
        List<? extends Range.Bucket> if10Buckets = impactFactor10.getBuckets();
        Range.Bucket if10 = if10Buckets.get(0);
        AggregationDto.ImpactFactor if10Dto = new AggregationDto.ImpactFactor();
        if10Dto.from = 10;
        if10Dto.docCount = if10.getDocCount();
        impactFactors.add(if10Dto);

        AggregationDto.ImpactFactor allIf = new AggregationDto.ImpactFactor();
        allIf.from = AggregationDto.ALL;
        allIf.to = AggregationDto.ALL;
        allIf.docCount = ifFiltered.getDocCount();
        impactFactors.add(allIf);

        return impactFactors;
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
