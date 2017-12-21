package network.pluto.absolute.service;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.WeightBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SearchService {

    private final RestHighLevelClient restHighLevelClient;

    @Autowired
    public SearchService(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    @Value("${pluto.server.es.index}")
    private String indexName;

    public Page<Long> search(String text, Pageable pageable) {
        SearchRequest request = new SearchRequest(indexName);

        SearchSourceBuilder builder = new SearchSourceBuilder();

        // search specific fields
        MultiMatchQueryBuilder query = QueryBuilders.multiMatchQuery(text, "title")
                .field("abstract", 3)
                .operator(Operator.AND)
                .analyzer("standard")
                .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
                .tieBreaker(0.5f);

        // venue booster
        ExistsQueryBuilder venueExistsQuery = QueryBuilders.existsQuery("venue");
        WeightBuilder weight = new WeightBuilder().setWeight(2);
        FunctionScoreQueryBuilder.FilterFunctionBuilder function = new FunctionScoreQueryBuilder.FilterFunctionBuilder(venueExistsQuery, weight);

        // set query
        FunctionScoreQueryBuilder scoredQuery = QueryBuilders.functionScoreQuery(query, new FunctionScoreQueryBuilder.FilterFunctionBuilder[] { function });
        builder.query(scoredQuery);

        // do not retrieve source
        builder.fetchSource(false);

        // apply pagination
        builder.from(pageable.getOffset());
        builder.size(pageable.getPageSize());

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
}
