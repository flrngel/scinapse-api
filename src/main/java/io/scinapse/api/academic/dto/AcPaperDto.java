package io.scinapse.api.academic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.data.academic.Paper;
import io.scinapse.api.data.academic.PaperAbstract;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class AcPaperDto {

    private long id;
    private String title;
    private Integer year;
    private LocalDate publishedDate;

    @JsonProperty("abstract")
    private String paperAbstract;

    private String doi;
    private String volume;
    private String issue;

    private int authorCount;
    private long referenceCount;
    private long citedCount;

    private AcJournalDto journal;
    private AcConferenceInstanceDto conferenceInstance;

    private List<AcPaperAuthorDto> authors = new ArrayList<>();
    private List<AcPaperFosDto> fosList = new ArrayList<>();
    private List<AcPaperUrlDto> urls = new ArrayList<>();

    public AcPaperDto(Paper paper, DetailSelector selector) {
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

        if (selector.loadJournal) {
            Optional.ofNullable(paper.getJournal())
                    .map(AcJournalDto::new)
                    .ifPresent(this::setJournal);
        }

        if (selector.loadConferenceInstance) {
            Optional.ofNullable(paper.getConferenceInstance())
                    .map(AcConferenceInstanceDto::new)
                    .ifPresent(this::setConferenceInstance);
        }

        if (selector.loadAbstract) {
            Optional.ofNullable(paper.getPaperAbstract())
                    .map(PaperAbstract::getAbstract)
                    .ifPresent(this::setPaperAbstract);
        }

        if (selector.loadAuthor && !CollectionUtils.isEmpty(paper.getAuthors())) {
            this.authors = paper.getAuthors()
                    .stream()
                    .map(AcPaperAuthorDto::new)
                    .sorted(Comparator.comparing(AcPaperAuthorDto::getOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        }

        if (selector.loadFos && !CollectionUtils.isEmpty(paper.getPaperFosList())) {
            this.fosList = paper.getPaperFosList()
                    .stream()
                    .map(AcPaperFosDto::new)
                    .collect(Collectors.toList());
        }

        if (selector.loadUrl && !CollectionUtils.isEmpty(paper.getPaperUrls())) {
            this.urls = paper.getPaperUrls()
                    .stream()
                    .map(AcPaperUrlDto::new)
                    .collect(Collectors.toList());
        }
    }

    @Builder
    public static class DetailSelector {
        private boolean loadJournal;
        private boolean loadConferenceInstance;
        private boolean loadAbstract;
        private boolean loadAuthor;
        private boolean loadFos;
        private boolean loadUrl;

        public static DetailSelector none() {
            return builder().build();
        }

        public static DetailSelector simple() {
            return builder()
                    .loadJournal(true)
                    .loadConferenceInstance(true)
                    .loadAuthor(true)
                    .build();
        }

        public static DetailSelector compact() {
            return builder()
                    .loadJournal(true)
                    .loadConferenceInstance(true)
                    .loadAuthor(true)
                    .loadUrl(true)
                    .build();
        }

        public static DetailSelector detail() {
            return builder()
                    .loadJournal(true)
                    .loadConferenceInstance(true)
                    .loadAuthor(true)
                    .loadUrl(true)
                    .loadAbstract(true)
                    .build();
        }

        public static DetailSelector full() {
            return builder()
                    .loadJournal(true)
                    .loadConferenceInstance(true)
                    .loadAuthor(true)
                    .loadUrl(true)
                    .loadAbstract(true)
                    .loadFos(true)
                    .build();
        }
    }

}
