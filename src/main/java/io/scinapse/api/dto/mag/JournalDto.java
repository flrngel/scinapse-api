package io.scinapse.api.dto.mag;

import io.scinapse.api.model.mag.Journal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
public class JournalDto {

    private long id;
    private String title;
    private String issn;
    private String webPage;
    private Double impactFactor;
    private Long paperCount;
    private Long citationCount;
    private List<JournalFosDto> fosList;

    @Deprecated
    private String fullTitle;

    public JournalDto(Journal journal) {
        this(journal, false);
    }

    public JournalDto(Journal journal, boolean loadFos) {
        this.id = journal.getId();
        this.title = journal.getTitle();
        this.issn = journal.getIssn();
        this.webPage = journal.getWebPage();
        this.impactFactor = journal.getImpactFactor();
        this.paperCount = journal.getPaperCount();
        this.citationCount = journal.getCitationCount();

        // deprecated
        this.fullTitle = journal.getTitle();

        if (loadFos && !CollectionUtils.isEmpty(journal.getFosList())) {
            this.fosList = journal.getFosList().stream().map(JournalFosDto::new).collect(Collectors.toList());
        }
    }

}
