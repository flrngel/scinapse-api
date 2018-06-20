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

    public PaperAuthorDto(network.pluto.bibliotheca.dtos.AuthorDto dto) {
        this.paperId = dto.getId();
        this.id = dto.getId();
        this.name = dto.getName();
        this.order = dto.getOrder();
        this.hIndex = dto.getHIndex();

        network.pluto.bibliotheca.dtos.AffiliationDto affiliation = dto.getAffiliation();
        if (affiliation != null) {
            AffiliationDto affiliationDto = new AffiliationDto();
            affiliationDto.setId(affiliation.getId());
            affiliationDto.setName(affiliation.getName());

            this.affiliation = affiliationDto;
            this.organization = affiliation.getName();
        }
    }

}
