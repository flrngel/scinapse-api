package io.scinapse.api.dto.v2;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.util.Query;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class EsPaperSearchResponse {

    private Query query;

    private SearchResponse paperResponse;
    private SearchResponse authorResponse;

    private List<EsPaper> esPapers;
    private long paperTotalHits;
    private Page<PaperItemDto> paperItemPage;

    private List<Long> authorIds;

    private final PaperSearchAdditional additional = new PaperSearchAdditional();

    public EsPaperSearchResponse(Query query, PageRequest pageRequest, MultiSearchResponse.Item[] responses) {
        this.query = query;

        this.setPaperResponse(responses[0].getResponse());

        this.authorResponse = responses[1].getResponse();
        this.authorIds = StreamSupport.stream(authorResponse.getHits().spliterator(), false)
                .map(SearchHit::getId)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    public EsPaperSearchResponse(Query query, SearchResponse paperResponse) {
        this.query = query;
        this.setPaperResponse(paperResponse);
    }

    private void setPaperResponse(SearchResponse paperResponse) {
        this.paperResponse = paperResponse;

        this.esPapers = StreamSupport.stream(paperResponse.getHits().spliterator(), false)
                .map(paper -> {
                    EsPaper esPaper = new EsPaper();
                    esPaper.paperId = Long.parseLong(paper.getId());

                    esPaper.titleHighlighted = Optional.ofNullable(paper.getHighlightFields().get("title.stemmed"))
                            .map(HighlightField::getFragments)
                            .map(frags -> frags[0])
                            .map(Text::string)
                            .orElse(null);

                    esPaper.abstractHighlighted = Optional.ofNullable(paper.getHighlightFields().get("abstract.stemmed"))
                            .map(HighlightField::getFragments)
                            .map(frags -> frags[0])
                            .map(Text::string)
                            .orElse(null);

                    return esPaper;
                })
                .collect(Collectors.toList());

        this.paperTotalHits = paperResponse.getHits().totalHits;
    }

    @Getter
    @Setter
    public class EsPaper {
        private long paperId;
        private String titleHighlighted;
        private String abstractHighlighted;
    }

}
