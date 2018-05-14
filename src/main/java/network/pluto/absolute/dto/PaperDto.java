package network.pluto.absolute.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.bibliotheca.models.mag.Paper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
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

    private String publisher;

    private String venue;

    private String volume;

    private String issue;

    private String pageStart;

    private String pageEnd;

    private long referenceCount = 0;

    private long citedCount = 0;

    private long authorCount = 0;

    private long fosCount = 0;

    private long urlCount = 0;

    private long commentCount = 0;

    private JournalDto journal;

    private List<PaperAuthorDto> authors = new ArrayList<>();

    private List<FosDto> fosList = new ArrayList<>();

    private List<PaperUrlDto> urls = new ArrayList<>();

    private List<CommentDto> comments = new ArrayList<>();

    public PaperDto(Paper paper) {
        this(paper, true);

        if (paper.getPaperAbstract() != null) {
            this.paperAbstract = paper.getPaperAbstract().getAbstract();
        }

        if (!paper.getPaperFosList().isEmpty()) {
            this.fosList = paper.getPaperFosList().stream().map(FosDto::new).collect(Collectors.toList());
            this.fosCount = this.fosList.size();
        }

        if (!paper.getPaperUrls().isEmpty()) {
            this.urls = paper.getPaperUrls().stream().map(PaperUrlDto::new).collect(Collectors.toList());
            this.urlCount = this.urls.size();
        }
    }

    private PaperDto(Paper paper, boolean simple) {
        this.id = paper.getId();
        this.title = paper.getOriginalTitle();
        this.year = paper.getYear();
        this.doi = paper.getDoi();
        this.publisher = paper.getPublisher();
        this.volume = paper.getVolume();
        this.issue = paper.getIssue();
        this.referenceCount = paper.getPaperCount() != null ? paper.getPaperCount() : 0;
        this.citedCount = paper.getCitationCount() != null ? paper.getCitationCount() : 0;

        if (paper.getJournal() != null) {
            this.journal = new JournalDto(paper.getJournal());
        }

        if (!paper.getPaperAuthorAffiliations().isEmpty()) {
            this.authors = paper.getPaperAuthorAffiliations()
                    .stream()
                    .map(PaperAuthorDto::new)
                    .sorted(Comparator.comparing(PaperAuthorDto::getOrder))
                    .collect(Collectors.toList());
            this.authorCount = this.authors.size();
        }
    }

    public static PaperDto simple(Paper paper) {
        return new PaperDto(paper, true);
    }

}
