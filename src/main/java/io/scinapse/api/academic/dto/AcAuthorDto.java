package io.scinapse.api.academic.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.data.academic.Author;
import io.scinapse.api.data.academic.AuthorHIndex;
import io.scinapse.api.dto.mag.AffiliationDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class AcAuthorDto {

    private long id;
    private String name;
    private AffiliationDto lastKnownAffiliation;
    private Integer hIndex;
    private long paperCount;
    private long citationCount;

    public AcAuthorDto(Author author) {
        this.id = author.getId();
        this.name = author.getName();
        this.paperCount = author.getPaperCount();
        this.citationCount = author.getCitationCount();

        Optional.ofNullable(author.getLastKnownAffiliation())
                .map(AffiliationDto::new)
                .ifPresent(this::setLastKnownAffiliation);

        Optional.ofNullable(author.getAuthorHIndex())
                .map(AuthorHIndex::getHIndex)
                .ifPresent(this::setHIndex);
    }

}
