package io.scinapse.api.dto.mag;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.api.data.academic.Author;
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
    private long paperCount;

    @JsonProperty("citation_count")
    private long citationCount;

    @JsonProperty("hindex")
    private Integer hIndex;

    private String email;

    @JsonProperty("is_email_hidden")
    private boolean emailHidden;

    private String bio;

    @JsonProperty("web_page")
    private String webPage;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("is_layered")
    private boolean layered = false;

    @JsonProperty("representative_papers")
    private List<PaperDto> representativePapers = new ArrayList<>();

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

        if (author.getLastKnownAffiliation() != null) {
            this.lastKnownAffiliation = new AffiliationDto(author.getLastKnownAffiliation());
        }
    }

}
