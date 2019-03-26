package io.scinapse.api.dto.mag;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.api.academic.dto.AcConferenceInstanceDto;
import io.scinapse.domain.data.academic.model.Paper;
import io.scinapse.api.dto.CommentDto;
import io.scinapse.api.dto.PaperImageDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class PaperDto {

    private long id;

    private String title;

    private Integer year;

    @JsonProperty("published_date")
    private LocalDate publishedDate;

    @JsonProperty("abstract")
    private String paperAbstract;

    private String lang;

    private String doi;

    private String volume;

    private String issue;

    private String pageStart;

    private String pageEnd;

    @JsonProperty("author_count")
    private int authorCount = 0;

    private long referenceCount = 0;

    private long citedCount = 0;

    private long fosCount = 0;

    private long urlCount = 0;

    private long commentCount = 0;

    private JournalDto journal;

    @JsonProperty("conference_instance")
    private AcConferenceInstanceDto conferenceInstance;

    private List<PaperAuthorDto> authors = new ArrayList<>();

    private List<PaperFosDto> fosList = new ArrayList<>();

    private List<PaperUrlDto> urls = new ArrayList<>();

    private List<PaperImageDto> images = new ArrayList<>();

    private List<CommentDto> comments = new ArrayList<>();

    private PaperDto(Paper paper) {
        this.id = paper.getId();
        this.title = paper.getTitle();
        this.year = paper.getYear();
        this.publishedDate = paper.getPublishedDate();
        this.doi = paper.getDoi();
        this.volume = paper.getVolume();
        this.issue = paper.getIssue();
        this.authorCount = paper.getAuthorCount();
        this.referenceCount = paper.getReferenceCount();
        this.citedCount = paper.getCitationCount();
    }

    public static PaperDto of(Paper paper) {
        return new PaperDto(paper);
    }

}
