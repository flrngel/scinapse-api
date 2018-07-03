package network.pluto.absolute.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.bibliotheca.models.mag.Journal;

@NoArgsConstructor
@Getter
@Setter
public class JournalDto {

    private long id;
    private String fullTitle;
    private Double impactFactor;
    private long paperCount;

    public JournalDto(Journal journal) {
        this.id = journal.getId();
        this.fullTitle = journal.getDisplayName();
        this.impactFactor = journal.getImpactFactor();
        this.paperCount = journal.getPaperCount();
    }

}
