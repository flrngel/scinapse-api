package io.scinapse.api.academic.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.data.academic.Journal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class AcJournalDto {

    private long id;
    private String title;
    private String issn;
    private String webPage;
    private Double impactFactor;
    private long paperCount;
    private long citationCount;

    public AcJournalDto(Journal journal) {
        this.id = journal.getId();
        this.title = journal.getTitle();
        this.issn = journal.getIssn();
        this.webPage = journal.getWebPage();
        this.impactFactor = journal.getImpactFactor();
        this.paperCount = journal.getPaperCount();
        this.citationCount = journal.getCitationCount();
    }

}
