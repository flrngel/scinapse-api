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
    private PageRequest pageRequest;

    private SearchResponse paperResponse;
    private SearchResponse authorResponse;

    private List<EsPaper> esPapers;
    private long paperTotalHits;
    private Page<PaperItemDto> paperItemPage;
    private List<Long> topHits;
    private List<Long> topRefPaperIds;

    private List<Long> authorIds;
    private long authorTotalHits;

    private final PaperSearchAdditional additional = new PaperSearchAdditional();

    public EsPaperSearchResponse(Query query, PageRequest pageRequest, MultiSearchResponse.Item[] responses) {
        this.query = query;
        this.pageRequest = pageRequest;

        this.setPaperResponse(responses[0].getResponse());
        this.setAuthorResponse(responses[1].getResponse());
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

                    esPaper.titleHighlighted = Optional.ofNullable(paper.getHighlightFields().get("title.shingles"))
                            .map(HighlightField::getFragments)
                            .map(frags -> frags[0])
                            .map(Text::string)
                            .orElse(null);

                    esPaper.abstractHighlighted = Optional.ofNullable(paper.getHighlightFields().get("abstract.shingles"))
                            .map(HighlightField::getFragments)
                            .map(frags -> frags[0])
                            .map(Text::string)
                            .orElse(null);

                    return esPaper;
                })
                .collect(Collectors.toList());

        this.paperTotalHits = paperResponse.getHits().totalHits;
    }

    private void setAuthorResponse(SearchResponse authorResponse) {
        this.authorResponse = authorResponse;

        this.authorIds = StreamSupport.stream(authorResponse.getHits().spliterator(), false)
                .map(SearchHit::getId)
                .map(Long::parseLong)
                .collect(Collectors.toList());

        this.authorTotalHits = authorResponse.getHits().getTotalHits();
    }

    @Getter
    @Setter
    public static class EsPaper {
        private long paperId;
        private String titleHighlighted;
        private String abstractHighlighted;
    }

}
