package io.scinapse.api.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.google.common.base.Preconditions;
import io.scinapse.api.dto.AggregationDto;
import io.scinapse.api.dto.CompletionDto;
import io.scinapse.api.dto.SuggestionDto;
import io.scinapse.api.enums.CompletionType;
import io.scinapse.api.repository.mag.FieldsOfStudyRepository;
import io.scinapse.api.repository.mag.JournalRepository;
import io.scinapse.api.util.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.*;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregator;
import org.elasticsearch.search.aggregations.bucket.sampler.Sampler;
import org.elasticsearch.search.aggregations.bucket.sampler.SamplerAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.suggest.SortBy;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.phrase.DirectCandidateGeneratorBuilder;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestionBuilder;
import org.elasticsearch.search.suggest.term.TermSuggestionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@XRayEnabled
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

    @Value("${pluto.server.es.index.suggestion.fos}")
    private String fosSuggestionIndex;

    @Value("${pluto.server.es.index.suggestion.title}")
    private String titleSuggestionIndex;

    private static final String SAMPLE_AGG_NAME = "sample";
    private static final String JOURNAL_AGG_NAME = "journal";
    private static final String FOS_AGG_NAME = "fos";
    private static final String YEAR_AGG_NAME = "year";
    private static final String IF_AGG_NAME = "if";
    private static final String IF_10_AGG_NAME = "if10";

    public Page<Long> search(Query query, Pageable pageable) {
        Preconditions.checkNotNull(query);
        Preconditions.checkNotNull(pageable);

        SearchRequest request = new SearchRequest(indexName);

        SearchSourceBuilder builder = SearchSourceBuilder.searchSource();

        // set query
        builder.query(query.toQuery());

        // do not retrieve source
        builder.fetchSource(false);

        // apply pagination
        builder.from(pageable.getOffset());
        builder.size(pageable.getPageSize());

        if (query.shouldRescore()) {
            builder.addRescorer(query.getPhraseRescoreQuery());
            builder.addRescorer(query.getFunctionRescoreQuery());
        }

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

    public Page<Long> searchWithSort(Query query, List<SortBuilder> sorts, Pageable pageable) {
        SearchSourceBuilder builder = SearchSourceBuilder.searchSource()
                .query(query.toSortQuery())
                .fetchSource(false)
                .from(pageable.getOffset())
                .size(pageable.getPageSize());

        sorts.forEach(builder::sort);

        try {
            SearchRequest request = new SearchRequest(indexName).source(builder);
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
        MatchQueryBuilder query = QueryBuilders.matchQuery("title.keyword", journalTitle);

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

    public List<CompletionDto> complete(String keyword) {
        MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("name", keyword).operator(Operator.AND);
        ConstantScoreQueryBuilder constantQuery = QueryBuilders.constantScoreQuery(matchQuery);
        FunctionScoreQueryBuilder functionQuery = QueryBuilders.functionScoreQuery(
                constantQuery,
                new FieldValueFactorFunctionBuilder("paper_count")
                        .modifier(FieldValueFactorFunction.Modifier.LOG2P))
                .boostMode(CombineFunction.REPLACE);

        SearchSourceBuilder source = SearchSourceBuilder.searchSource()
                .query(functionQuery)
                .fetchSource("name", null);

        try {
            SearchRequest request = new SearchRequest(fosSuggestionIndex).source(source);
            SearchResponse response = restHighLevelClient.search(request);

            List<CompletionDto> dtos = new ArrayList<>();
            for (SearchHit hit : response.getHits()) {
                Object name = hit.getSourceAsMap().get("name");
                if (name == null) {
                    continue;
                }
                dtos.add(new CompletionDto((String) name, CompletionType.FOS));
            }

            return dtos.stream().distinct().collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch exception", e);
        }
    }

    public SuggestionDto suggest(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return null;
        }

        DirectCandidateGeneratorBuilder candidate = new DirectCandidateGeneratorBuilder("title")
                .suggestMode(TermSuggestionBuilder.SuggestMode.POPULAR.name())
                .size(10)
                .maxInspections(10)
                .sort(SortBy.FREQUENCY.name());

        if (keyword.trim().split(" ").length == 1) {
            candidate.maxTermFreq(300);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("field_name", "title");

        PhraseSuggestionBuilder phraseSuggest = SuggestBuilders.phraseSuggestion("title")
                .text(keyword)
                .addCandidateGenerator(candidate)
                .size(1)
                .maxErrors(3)
                .highlight("<b>", "</b>")
                .collateQuery("{\"match\": {\"{{field_name}}\": {\"query\": \"{{suggestion}}\", \"operator\": \"and\"}}}")
                .collateParams(params);

        SuggestBuilder suggest = new SuggestBuilder()
                .addSuggestion("suggest", phraseSuggest);

        SearchSourceBuilder source = SearchSourceBuilder.searchSource()
                .size(0)
                .fetchSource(false)
                .suggest(suggest);

        try {
            SearchRequest request = new SearchRequest(titleSuggestionIndex).source(source);
            SearchResponse response = restHighLevelClient.search(request);

            List<? extends Suggest.Suggestion.Entry.Option> options = response
                    .getSuggest()
                    .getSuggestion("suggest")
                    .getEntries().get(0)
                    .getOptions();

            if (options.size() == 0) {
                return null;
            }

            return new SuggestionDto(keyword, options.get(0).getText().string(), options.get(0).getHighlighted().string());
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
        dto.journals.sort(Comparator.comparing(j -> j.impactFactor, Comparator.nullsLast(Comparator.reverseOrder())));

        dto.fosList.forEach(f -> {
            Filters.Bucket bucket = fos.getBucketByKey(String.valueOf(f.id));
            if (bucket == null) {
                return;
            }
            f.docCount = bucket.getDocCount();
        });
        dto.fosList.sort(Comparator.comparing(f -> f.docCount, Comparator.reverseOrder()));
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

        return journalRepository.findByIdIn(journalIds)
                .stream()
                .map(j -> {
                    AggregationDto.Journal journalDto = new AggregationDto.Journal();
                    journalDto.id = j.getId();
                    journalDto.title = j.getDisplayName();
                    journalDto.impactFactor = j.getImpactFactor();
                    return journalDto;
                })
                .collect(Collectors.toList());
    }

    private List<AggregationDto.Fos> getFosList(Map<String, Aggregation> aggregationMap) {
        Terms fos = (Terms) aggregationMap.get(FOS_AGG_NAME);
        List<Long> fosIds = fos.getBuckets()
                .stream()
                .map(f -> (long) f.getKey())
                .collect(Collectors.toList());
        return fieldsOfStudyRepository.findByIdIn(fosIds)
                .stream()
                .map(f -> {
                    AggregationDto.Fos fosDto = new AggregationDto.Fos();
                    fosDto.id = f.getId();
                    fosDto.name = f.getDisplayName();
                    return fosDto;
                })
                .collect(Collectors.toList());
    }

}
