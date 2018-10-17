package io.scinapse.api.dto.mag;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.api.model.mag.Author;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class AuthorDto {

    private long id;
    private String name;

    @JsonProperty("last_known_affiliation")
    private AffiliationDto lastKnownAffiliation;

    @JsonProperty("paper_count")
    private Long paperCount;

    @JsonProperty("citation_count")
    private Long citationCount;

    @JsonProperty("hindex")
    private Integer hIndex;

    @JsonProperty("profile_id")
    private String profileId;

    @JsonProperty("top_papers")
    private List<PaperDto> topPapers = new ArrayList<>();

    public AuthorDto(Author author) {
        this.id = author.getId();
        this.name = author.getName();
        this.paperCount = author.getPaperCount();
        this.citationCount = author.getCitationCount();

        if (author.getLastKnownAffiliation() != null) {
            this.lastKnownAffiliation = new AffiliationDto(author.getLastKnownAffiliation());
        }

        if (author.getAuthorHIndex() != null) {
            this.hIndex = author.getAuthorHIndex().getHIndex();
        }

        this.profileId = author.getProfileId();
    }

    @JsonGetter("is_profile_connected")
    public boolean profileConnected() {
        return StringUtils.isNotBlank(this.profileId);
    }

}
