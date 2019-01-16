package io.scinapse.api.academic.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.data.academic.AuthorHIndex;
import io.scinapse.api.data.academic.PaperAuthor;
import io.scinapse.api.data.academic.PaperTopAuthor;
import io.scinapse.api.dto.mag.AffiliationDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class AcPaperAuthorDto {

    private long id;
    private String name;
    private AffiliationDto affiliation;
    private Integer hIndex;
    private long paperCount;
    private long citationCount;
    private Integer order;

    public AcPaperAuthorDto(PaperAuthor relation) {
        this.id = relation.getAuthor().getId();
        this.name = relation.getAuthor().getName();
        this.paperCount = relation.getAuthor().getPaperCount();
        this.citationCount = relation.getAuthor().getCitationCount();
        this.order = relation.getAuthorSequenceNumber();

        Optional.ofNullable(relation.getAffiliation())
                .map(AffiliationDto::new)
                .ifPresent(this::setAffiliation);

        Optional.ofNullable(relation.getAuthor().getAuthorHIndex())
                .map(AuthorHIndex::getHIndex)
                .ifPresent(this::setHIndex);
    }

    public AcPaperAuthorDto(PaperTopAuthor relation) {
        this.id = relation.getAuthor().getId();
        this.name = relation.getAuthor().getName();
        this.paperCount = relation.getAuthor().getPaperCount();
        this.citationCount = relation.getAuthor().getCitationCount();
        this.order = relation.getAuthorSequenceNumber();

        Optional.ofNullable(relation.getAffiliation())
                .map(AffiliationDto::new)
                .ifPresent(this::setAffiliation);

        Optional.ofNullable(relation.getAuthor().getAuthorHIndex())
                .map(AuthorHIndex::getHIndex)
                .ifPresent(this::setHIndex);
    }

}
