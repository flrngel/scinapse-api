package io.scinapse.api.dto.mag;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.api.model.mag.PaperAuthor;
import io.scinapse.api.model.mag.PaperTopAuthor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

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

    @JsonProperty("profile_id")
    private String profileId;


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

        this.profileId = relation.getAuthor().getProfileId();
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

    @JsonGetter("is_profile_connected")
    public boolean profileConnected() {
        return StringUtils.isNotBlank(this.profileId);
    }

}
