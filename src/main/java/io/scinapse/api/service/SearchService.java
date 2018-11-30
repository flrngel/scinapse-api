package io.scinapse.api.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.google.common.base.Preconditions;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.data.academic.repository.FieldsOfStudyRepository;
import io.scinapse.api.data.academic.repository.JournalRepository;
import io.scinapse.api.dto.AggregationDto;
import io.scinapse.api.dto.CompletionDto;
import io.scinapse.api.dto.CompletionResponseDto;
import io.scinapse.api.dto.SuggestionDto;
import io.scinapse.api.enums.CompletionType;
import io.scinapse.api.util.JsonUtils;
import io.scinapse.api.util.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScriptScoreFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
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
import org.elasticsearch.search.rescore.QueryRescoreMode;
import org.elasticsearch.search.rescore.QueryRescorerBuilder;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
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
    private final RestTemplate restTemplate;

    @Value("${pluto.server.es.index}")
    private String indexName;

    @Value("${pluto.server.es.index.suggestion.fos}")
    private String fosSuggestionIndex;

    @Value("${pluto.server.es.index.suggestion.title}")
    private String titleSuggestionIndex;

    @Value("${pluto.server.es.index.author}")
    private String authorIndex;

    @Value("${pluto.server.scholar.url}")
    private String scholarUrl;

    @Value("${pluto.server.es.index.suggestion.affiliation}")
    private String affiliationSuggestionIndex;


    private static final String SAMPLE_AGG_NAME = "sample";
    private static final String JOURNAL_AGG_NAME = "journal";
    private static final String FOS_AGG_NAME = "fos";
    private static final String YEAR_AGG_NAME = "year";
    private static final String IF_AGG_NAME = "if";
    private static final String IF_10_AGG_NAME = "if10";

    public Page<Long> search(Query query, PageRequest pageRequest) {
        Preconditions.checkNotNull(query);
        Preconditions.checkNotNull(pageRequest);

        SearchRequest request = new SearchRequest(indexName);

        SearchSourceBuilder builder = SearchSourceBuilder.searchSource();

        // set query
        builder.query(query.toQuery());

        // do not retrieve source
        builder.fetchSource(false);

        // apply pagination
        builder.from(pageRequest.getOffset());
        builder.size(pageRequest.getSize());

        if (query.shouldRescore()) {
            builder.addRescorer(query.getPhraseRescoreQuery());
            builder.addRescorer(query.getCitationRescoreQuery());
            builder.addRescorer(query.getAbsenceRescoreQuery());
        }

        request.source(builder);

        try {
            SearchResponse response = restHighLevelClient.search(request);

            List<Long> list = new ArrayList<>();
            for (SearchHit hit : response.getHits()) {
                list.add(Long.valueOf(hit.getId()));
            }
            return new PageImpl<>(list, pageRequest.toPageable(), response.getHits().getTotalHits());

        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Elasticsearch exception", e);
        }
    }

    public Page<Long> searchWithSort(Query query, List<SortBuilder> sorts, PageRequest pageRequest) {
        SearchSourceBuilder builder = SearchSourceBuilder.searchSource()
                .query(query.toSortQuery())
                .fetchSource(false)
                .from(pageRequest.getOffset())
                .size(pageRequest.getSize());

        sorts.forEach(builder::sort);

        try {
            SearchRequest request = new SearchRequest(indexName).source(builder);
            SearchResponse response = restHighLevelClient.search(request);

            List<Long> list = new ArrayList<>();
            for (SearchHit hit : response.getHits()) {
                list.add(Long.valueOf(hit.getId()));
            }
            return new PageImpl<>(list, pageRequest.toPageable(), response.getHits().getTotalHits());
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Elasticsearch exception", e);
        }
    }

    public Page<Long> searchAuthor(String keyword, PageRequest pageRequest) {
        BoolQueryBuilder authorQuery = QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("name", keyword).operator(Operator.AND).boost(2))
                .should(QueryBuilders.matchQuery("name", keyword).minimumShouldMatch("2").boost(2))
                .should(QueryBuilders.matchQuery("name.metaphone", keyword))
                .should(QueryBuilders.matchQuery("name.porter", keyword))
                .should(QueryBuilders.matchQuery("affiliation.name", keyword).boost(2));


        // author paper/citation count booster script
        Script script = new Script("Math.log10(doc['paper_count'].value + doc['citation_count'].value + 10)");
        ScriptScoreFunctionBuilder boostFunction = new ScriptScoreFunctionBuilder(script);

        FunctionScoreQueryBuilder rescoreQuery = QueryBuilders
                .functionScoreQuery(boostFunction)
                .maxBoost(2); // limit boosting

        QueryRescorerBuilder rescorer = new QueryRescorerBuilder(rescoreQuery)
                .windowSize(10)
                .setScoreMode(QueryRescoreMode.Multiply);

        SearchSourceBuilder source = SearchSourceBuilder.searchSource()
                .query(authorQuery)
                .addRescorer(rescorer)
                .fetchSource(false)
                .from(pageRequest.getOffset())
                .size(pageRequest.getSize());

        try {
            SearchRequest request = new SearchRequest(authorIndex).source(source);
            SearchResponse response = restHighLevelClient.search(request);

            List<Long> list = new ArrayList<>();
            for (SearchHit hit : response.getHits()) {
                list.add(Long.valueOf(hit.getId()));
            }
            return new PageImpl<>(list, pageRequest.toPageable(), response.getHits().getTotalHits());
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Elasticsearch exception", e);
        }
    }

    public List<CompletionDto> completeByScholar(String keyword) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(scholarUrl)
                .queryParam("q", keyword)
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "PostmanRuntime/7.3.0");
        HttpEntity<Object> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            String completionJson = responseEntity.getBody();

            CompletionResponseDto dto = JsonUtils.fromJson(completionJson, CompletionResponseDto.class);
            return dto.getCompletions()
                    .stream()
                    .map(c -> {
                        String replaced = StringUtils.replace(c, "|", "");
                        return new CompletionDto(replaced, CompletionType.KEYWORD);
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Fail to retrieve completion keywords: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
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

    public List<CompletionDto> completeAffiliation(String keyword) {
        MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("name", keyword).operator(Operator.AND);
        ConstantScoreQueryBuilder constantQuery = QueryBuilders.constantScoreQuery(matchQuery);

        FieldValueFactorFunctionBuilder paperBoost = new FieldValueFactorFunctionBuilder("paper_count").modifier(FieldValueFactorFunction.Modifier.LOG2P);
        FunctionScoreQueryBuilder.FilterFunctionBuilder paperFunction = new FunctionScoreQueryBuilder.FilterFunctionBuilder(paperBoost);
        FieldValueFactorFunctionBuilder citationBoost = new FieldValueFactorFunctionBuilder("citation_count").modifier(FieldValueFactorFunction.Modifier.LOG2P);
        FunctionScoreQueryBuilder.FilterFunctionBuilder citationFunction = new FunctionScoreQueryBuilder.FilterFunctionBuilder(citationBoost);

        FunctionScoreQueryBuilder functionQuery = QueryBuilders.functionScoreQuery(
                constantQuery,
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[] { paperFunction, citationFunction })
                .boostMode(CombineFunction.REPLACE);

        SearchSourceBuilder source = SearchSourceBuilder.searchSource()
                .query(functionQuery)
                .fetchSource("name", null);

        try {
            SearchRequest request = new SearchRequest(affiliationSuggestionIndex).source(source);
            SearchResponse response = restHighLevelClient.search(request);

            List<CompletionDto> dtos = new ArrayList<>();
            for (SearchHit hit : response.getHits()) {
                Object name = hit.getSourceAsMap().get("name");
                if (name == null) {
                    continue;
                }
                CompletionDto dto = new CompletionDto((String) name, CompletionType.AFFILIATION);
                dto.additionalInfo.put("affiliation_id", Long.parseLong(hit.getId()));
                dtos.add(dto);
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

        SearchSourceBuilder suggestSource = SearchSourceBuilder.searchSource()
                .size(0)
                .fetchSource(false)
                .suggest(suggest);


        try {
            SearchRequest suggestRequest = new SearchRequest(titleSuggestionIndex).source(suggestSource);
            SearchRequest authorNameRequest = getAuthorNameRequest(keyword);

            MultiSearchRequest multiSearchRequest = new MultiSearchRequest()
                    .add(authorNameRequest)
                    .add(suggestRequest);
            MultiSearchResponse.Item[] responses = restHighLevelClient.multiSearch(multiSearchRequest).getResponses();

            if (responses.length != 2) {
                log.error("Size of suggest multi search is not two: " + responses.length);
                return null;
            }

            // do not suggest if the keyword matches specific author name.
            long totalHits = responses[0].getResponse().getHits().totalHits;
            if (totalHits > 0) {
                return null;
            }

            SearchResponse response = responses[1].getResponse();
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

    private SearchRequest getAuthorNameRequest(String keyword) {
        MatchQueryBuilder nameQuery = QueryBuilders.matchQuery("name", keyword).operator(Operator.AND);
        SearchSourceBuilder nameQuerySource = SearchSourceBuilder.searchSource()
                .size(0)
                .fetchSource(false)
                .query(nameQuery);
        return new SearchRequest(authorIndex).source(nameQuerySource);
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
                    journalDto.title = j.getTitle();
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
                    fosDto.name = f.getName();
                    return fosDto;
                })
                .collect(Collectors.toList());
    }

}
