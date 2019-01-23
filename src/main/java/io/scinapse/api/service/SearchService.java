package io.scinapse.api.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.dto.CompletionDto;
import io.scinapse.api.dto.CompletionResponseDto;
import io.scinapse.api.enums.CompletionType;
import io.scinapse.api.enums.PaperSort;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.util.JsonUtils;
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
import org.elasticsearch.index.query.functionscore.ScriptScoreFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.rescore.QueryRescoreMode;
import org.elasticsearch.search.rescore.QueryRescorerBuilder;
import org.elasticsearch.search.sort.SortBuilder;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@XRayEnabled
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final RestHighLevelClient restHighLevelClient;
    private final RestTemplate restTemplate;

    @Value("${pluto.server.es.index}")
    private String indexName;

    @Value("${pluto.server.es.index.suggestion.fos}")
    private String fosSuggestionIndex;

    @Value("${pluto.server.es.index.author}")
    private String authorIndex;

    @Value("${pluto.server.scholar.url}")
    private String scholarUrl;

    @Value("${pluto.server.es.index.suggestion.affiliation}")
    private String affiliationSuggestionIndex;

    public Page<Long> searchJournalPaper(long journalId, String queryText, PageRequest pageRequest) {
        if (StringUtils.isBlank(queryText)) {
            return searchJournalPaper(journalId, pageRequest);
        }

        Query parse = Query.parse(queryText);
        if (!parse.isValid()) {
            throw new BadRequestException("Invalid query: too short or long query text : " + queryText);
        }

        SortBuilder sortBuilder = PaperSort.toSortBuilder(pageRequest.getSort());

        BoolQueryBuilder query;
        if (sortBuilder != null) {
            query = parse.toSortQuery();
        } else {
            query = parse.toRelevanceQuery();
        }
        query.filter(QueryBuilders.termQuery("journal.id", journalId));

        SearchSourceBuilder source = SearchSourceBuilder.searchSource()
                .query(query)
                .fetchSource(false)
                .from(pageRequest.getOffset())
                .size(pageRequest.getSize());

        if (sortBuilder != null) {
            source.sort(sortBuilder);
        } else {
            source.addRescorer(parse.getPhraseRescoreQuery());
            source.addRescorer(parse.getCitationRescoreQuery());
            source.addRescorer(parse.getYearRescoreQuery());
            source.addRescorer(parse.getAbsenceRescoreQuery());
        }

        return searchAndExtractId(indexName, source, pageRequest);
    }

    public Page<Long> searchJournalPaper(long journalId, PageRequest pageRequest) {
        PaperSort sort = PaperSort.find(pageRequest.getSort());
        if (sort == null) {
            sort = PaperSort.NEWEST_FIRST;
        }
        SortBuilder sortBuilder = PaperSort.toSortBuilder(sort);

        BoolQueryBuilder query = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("journal.id", journalId));

        SearchSourceBuilder source = SearchSourceBuilder.searchSource()
                .query(query)
                .fetchSource(false)
                .from(pageRequest.getOffset())
                .size(pageRequest.getSize())
                .sort(sortBuilder);

        return searchAndExtractId(indexName, source, pageRequest);
    }

    public Page<Long> searchAuthor(String queryText, PageRequest pageRequest) {
        BoolQueryBuilder authorQuery = QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("name", queryText).operator(Operator.AND).boost(2))
                .should(QueryBuilders.matchQuery("name", queryText).minimumShouldMatch("2").boost(2))
                .should(QueryBuilders.matchQuery("name.metaphone", queryText))
                .should(QueryBuilders.matchQuery("name.porter", queryText))
                .should(QueryBuilders.matchQuery("affiliation.name", queryText).boost(2));

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

        return searchAndExtractId(authorIndex, source, pageRequest);
    }

    public Page<Long> searchAuthorPaper(Query query, PageRequest pageRequest) {
        SearchSourceBuilder builder = SearchSourceBuilder.searchSource()
                .query(query.toTitleQuery())
                .fetchSource(false)
                .from(pageRequest.getOffset())
                .size(pageRequest.getSize());

        return searchAndExtractId(indexName, builder, pageRequest);
    }

    private Page<Long> searchAndExtractId(String indexName, SearchSourceBuilder builder, PageRequest pageRequest) {
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

}
