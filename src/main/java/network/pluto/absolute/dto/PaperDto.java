package network.pluto.absolute.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.bibliotheca.models.Paper;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
public class PaperDto {

    private long id;

    private String magId;

    private String title;

    private int year;

    private Integer citation;

    @JsonProperty("abstract")
    private String paperAbstract;

    private String lang;

    private String doi;

    private String publisher;

    private String venue;

    private Integer volume;

    private Integer issue;

    private String pageStart;

    private String pageEnd;

    private List<FosDto> fosList;

    private List<PaperAuthorDto> authors;

    private List<CommentDto> comments;

    private long commentCount;

    public PaperDto(Paper paper) {
        this.id = paper.getId();
        this.magId = paper.getMagId();
        this.title = paper.getTitle();
        this.year = paper.getYear();
        this.citation = paper.getCitation();
        this.paperAbstract = paper.getPaperAbstract();
        this.lang = paper.getLang();
        this.doi = paper.getDoi();
        this.publisher = paper.getPublisher();
        this.venue = paper.getVenue();
        this.volume = paper.getVolume();
        this.issue = paper.getIssue();
        this.pageStart = paper.getPageStart();
        this.pageEnd = paper.getPageEnd();

        if (paper.getFosList() != null) {
            this.fosList = paper.getFosList().stream().map(FosDto::new).collect(Collectors.toList());
        }

        if (paper.getAuthors() != null) {
            this.authors = paper.getAuthors().stream().map(PaperAuthorDto::new).collect(Collectors.toList());
        }

        if (paper.getComments() != null) {
            this.comments = paper.getComments().stream().map(CommentDto::new).collect(Collectors.toList());
        }
    }
}
