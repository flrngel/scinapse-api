package io.scinapse.api.dto.mag;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.api.model.author.AuthorLayer;
import io.scinapse.api.model.mag.Author;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private String email;
    private String bio;
    private String webPage;

    @JsonProperty("is_layered")
    private boolean layered = false;

    @JsonProperty("selected_papers")
    private List<PaperDto> selectedPapers = new ArrayList<>();

    @JsonProperty("fos_list")
    private List<FosDto> fosList = new ArrayList<>();

    @JsonProperty("top_papers")
    private List<PaperDto> topPapers = new ArrayList<>();

    public AuthorDto(Author author) {
        this.id = author.getId();
        this.name = author.getName();
        this.paperCount = author.getPaperCount();
        this.citationCount = author.getCitationCount();

        if (author.getAuthorHIndex() != null) {
            this.hIndex = author.getAuthorHIndex().getHIndex();
        }

        if (author.getLayer() != null) {
            this.layered = true;
            this.name = author.getLayer().getName();
            this.paperCount = author.getLayer().getPaperCount();
            if (author.getLayer().getLastKnownAffiliation() != null) {
                this.lastKnownAffiliation = new AffiliationDto(author.getLayer().getLastKnownAffiliation());
            }
        } else {
            if (author.getLastKnownAffiliation() != null) {
                this.lastKnownAffiliation = new AffiliationDto(author.getLastKnownAffiliation());
            }
        }
    }

    public void putDetail(AuthorLayer layer) {
        this.email = layer.getEmail();
        this.bio = layer.getBio();
        this.webPage = layer.getWebPage();

        if (!CollectionUtils.isEmpty(layer.getFosList())) {
            this.fosList = layer.getFosList().stream().map(FosDto::new).collect(Collectors.toList());
        }
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
