package io.scinapse.api.dto.mag;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.api.model.mag.Author;
import io.scinapse.api.model.mag.PaperAuthor;
import io.scinapse.api.model.mag.PaperTopAuthor;
import io.scinapse.api.model.profile.ProfileAuthor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

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

        this.profileId = Optional.ofNullable(relation.getAuthor())
                .map(Author::getProfileAuthor)
                .map(ProfileAuthor::getId)
                .map(ProfileAuthor.ProfileAuthorId::getProfileId)
                .orElse(null);
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

        this.profileId = Optional.ofNullable(paperTopAuthor.getAuthor())
                .map(Author::getProfileAuthor)
                .map(ProfileAuthor::getId)
                .map(ProfileAuthor.ProfileAuthorId::getProfileId)
                .orElse(null);
    }

    @JsonGetter("is_profile_connected")
    public boolean profileConnected() {
        return StringUtils.isNotBlank(this.profileId);
    }

}
