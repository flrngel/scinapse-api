package io.scinapse.api.dto.mag;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.api.data.academic.Paper;
import io.scinapse.api.dto.CommentDto;
import io.scinapse.api.dto.PaperImageDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class PaperDto {

    private long id;

    private String title;

    private int year;

    @JsonProperty("abstract")
    private String paperAbstract;

    private String lang;

    private String doi;

    private String volume;

    private String issue;

    private String pageStart;

    private String pageEnd;

    private long referenceCount = 0;

    private long citedCount = 0;

    private long fosCount = 0;

    private long urlCount = 0;

    private long commentCount = 0;

    private JournalDto journal;

    private List<PaperAuthorDto> authors = new ArrayList<>();

    private List<PaperFosDto> fosList = new ArrayList<>();

    private List<PaperUrlDto> urls = new ArrayList<>();

    private List<PaperImageDto> images = new ArrayList<>();

    private List<CommentDto> comments = new ArrayList<>();

    private PaperDto(Paper paper) {
        this.id = paper.getId();
        this.title = paper.getTitle();
        this.year = paper.getYear();
        this.doi = paper.getDoi();
        this.volume = paper.getVolume();
        this.issue = paper.getIssue();
        this.referenceCount = paper.getReferenceCount() != null ? paper.getReferenceCount() : 0;
        this.citedCount = paper.getCitationCount() != null ? paper.getCitationCount() : 0;
    }

    public static PaperDto of(Paper paper) {
        return new PaperDto(paper);
    }

}
