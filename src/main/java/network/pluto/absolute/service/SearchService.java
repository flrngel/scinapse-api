package network.pluto.absolute.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import network.pluto.absolute.dto.search.PaperSearchDto;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    public List<PaperSearchDto> search(String text) {
        SearchRequest request = new SearchRequest(indexName);

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.multiMatchQuery(text, "title", "abstract"));
        builder.size(10);

        request.source(builder);

        try {
            SearchResponse response = restHighLevelClient.search(request);

            List<PaperSearchDto> list = new ArrayList<>();

            response.getHits().forEach(h -> {
                PaperSearchDto paperDto = null;
                try {
                    paperDto = objectMapper.readValue(h.getSourceAsString(), PaperSearchDto.class);
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
                list.add(paperDto);
            });

            return list;

        } catch (IOException e) {
            log.error(e.getMessage());
            return new ArrayList<>();
        }
    }
}
