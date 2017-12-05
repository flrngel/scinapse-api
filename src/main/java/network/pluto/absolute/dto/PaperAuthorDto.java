package network.pluto.absolute.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.bibliotheca.academic.PaperAuthor;

@NoArgsConstructor
@Getter
@Setter
public class PaperAuthorDto {
    private int ord;
    private String name;
    private String org;

    public PaperAuthorDto(PaperAuthor paperAuthor) {
        this.ord = paperAuthor.getOrd();
        this.name = paperAuthor.getName();
        this.org = paperAuthor.getOrg();
    }
}
