package io.scinapse.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.api.model.mag.Author;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private Integer hIndex;

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

}
