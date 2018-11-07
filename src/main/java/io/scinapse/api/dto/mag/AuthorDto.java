package io.scinapse.api.dto.mag;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.api.model.author.AuthorLayer;
import io.scinapse.api.model.mag.Author;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private String bio;

    @JsonProperty("selected_papers")
    private List<PaperDto> selectedPapers = new ArrayList<>();

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
    }

    public void putDetail(AuthorLayer layer) {
        this.bio = layer.getBio();
        this.paperCount = layer.getPaperCount();
    }

    @JsonGetter("profile_id")
    public String getProfileId() {
        return null;
    }

    @JsonGetter("is_profile_connected")
    public boolean profileConnected() {
        return false;
    }

}
