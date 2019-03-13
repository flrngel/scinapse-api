package io.scinapse.api.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.dto.CompletionDto;
import io.scinapse.api.dto.CompletionResponseDto;
import io.scinapse.domain.enums.CompletionType;
import io.scinapse.domain.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${pluto.server.scholar.url}")
    private String scholarUrl;

    @Value("${pluto.server.es.index.suggestion.affiliation}")
    private String affiliationSuggestionIndex;

    public List<CompletionDto> complete(String keyword) {
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
