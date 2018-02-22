package network.pluto.absolute.service;

import lombok.extern.slf4j.Slf4j;
import network.pluto.absolute.dto.JournalDto;
import network.pluto.absolute.util.Query;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
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

    @Value("${pluto.server.es.index.journal}")
    private String journalIndexName;

    public Page<Long> search(Query query, Pageable pageable) {
        SearchRequest request = new SearchRequest(indexName);

        SearchSourceBuilder builder = new SearchSourceBuilder();

        // set query
        builder.query(query.toQuery());

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

    public JournalDto searchJournal(String journalTitle) {
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

            SearchHit hit = hits.getAt(0);

            JournalDto dto = new JournalDto();
            dto.setId(Long.valueOf(hit.getId()));
            dto.setFullTitle(journalTitle);

            Object impactFactor = hit.getSource().get("impact_factor");
            if (impactFactor != null) {
                dto.setImpactFactor((Double) impactFactor);
            }

            return dto;
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Elasticsearch exception", e);
        }
    }

}
