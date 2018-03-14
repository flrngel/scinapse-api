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

    public JournalDto(Journal journal) {
        this.id = journal.getId();
        this.fullTitle = journal.getDisplayName();
    }

}
