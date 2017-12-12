package network.pluto.absolute.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.bibliotheca.models.PaperAuthor;

@NoArgsConstructor
@Getter
@Setter
public class PaperAuthorDto {

    private long paperId;
    private int order;
    private String name;
    private String organization;

    public PaperAuthorDto(PaperAuthor paperAuthor) {
        this.paperId = paperAuthor.getPaper().getId();
        this.order = paperAuthor.getOrder();
        this.name = paperAuthor.getName();
        this.organization = paperAuthor.getOrganization();
    }
}
