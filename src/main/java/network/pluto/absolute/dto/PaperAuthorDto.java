package network.pluto.absolute.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.bibliotheca.models.mag.PaperAuthorAffiliation;

@NoArgsConstructor
@Getter
@Setter
public class PaperAuthorDto {

    private long paperId;
    private long id;
    private String name;
    private Integer hIndex;
    private String organization;
    private AffiliationDto affiliation;
    private int order;

    public PaperAuthorDto(PaperAuthorAffiliation relation) {
        this.paperId = relation.getPaper().getId();
        this.id = relation.getAuthor().getId();
        this.name = relation.getAuthor().getDisplayName();
        this.order = relation.getAuthorSequenceNumber();

        if (relation.getAffiliation() != null) {
            this.affiliation = new AffiliationDto(relation.getAffiliation());
            this.organization = relation.getAffiliation().getDisplayName();
        }

        if (relation.getAuthor().getAuthorHIndex() != null) {
            this.hIndex = relation.getAuthor().getAuthorHIndex().getHIndex();
        }
    }

}
