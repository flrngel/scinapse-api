package network.pluto.absolute.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import network.pluto.absolute.dto.search.PaperSearchDto;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
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
    private final ObjectMapper objectMapper;

    @Autowired
    public SearchService(RestHighLevelClient restHighLevelClient, ObjectMapper objectMapper) {
        this.restHighLevelClient = restHighLevelClient;
        this.objectMapper = objectMapper;
    }

    @Value("${pluto.server.es.index}")
    private String indexName;

    public Page<PaperSearchDto> search(String text, Pageable pageable) {
        SearchRequest request = new SearchRequest(indexName);

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.multiMatchQuery(text, "title", "abstract"));
        builder.from(pageable.getOffset());
        builder.size(pageable.getPageSize());

        request.source(builder);

        try {
            SearchResponse response = restHighLevelClient.search(request);

            List<PaperSearchDto> list = new ArrayList<>();
            for (SearchHit hit : response.getHits()) {
                PaperSearchDto paperDto = objectMapper.readValue(hit.getSourceAsString(), PaperSearchDto.class);
                list.add(paperDto);
            }
            return new PageImpl<>(list, pageable, response.getHits().getTotalHits());

        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch exception", e);
        }
    }
}
