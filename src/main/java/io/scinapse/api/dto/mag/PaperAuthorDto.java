package io.scinapse.api.dto.mag;

import io.scinapse.api.model.mag.PaperAuthor;
import io.scinapse.api.model.mag.PaperTopAuthor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    public PaperAuthorDto(PaperAuthor relation) {
        this.paperId = relation.getPaperId();
        this.id = relation.getAuthor().getId();
        this.name = relation.getAuthor().getName();
        this.order = relation.getAuthorSequenceNumber();

        if (relation.getAffiliation() != null) {
            this.affiliation = new AffiliationDto(relation.getAffiliation());
            this.organization = relation.getAffiliation().getName();
        }

        if (relation.getAuthor().getAuthorHIndex() != null) {
            this.hIndex = relation.getAuthor().getAuthorHIndex().getHIndex();
        }
    }

    public PaperAuthorDto(PaperTopAuthor paperTopAuthor) {
        this.paperId = paperTopAuthor.getPaperId();
        this.id = paperTopAuthor.getId().getAuthorId();
        this.name = paperTopAuthor.getAuthor().getName();

        if (paperTopAuthor.getAffiliation() != null) {
            this.affiliation = new AffiliationDto(paperTopAuthor.getAffiliation());
            this.organization = paperTopAuthor.getAffiliation().getName();
        }

        if (paperTopAuthor.getAuthor().getAuthorHIndex() != null) {
            this.hIndex = paperTopAuthor.getAuthor().getAuthorHIndex().getHIndex();
        }
    }

}
