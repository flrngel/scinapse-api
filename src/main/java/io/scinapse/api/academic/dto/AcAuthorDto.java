package io.scinapse.api.academic.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.data.academic.Author;
import io.scinapse.api.data.academic.AuthorHIndex;
import io.scinapse.api.dto.mag.AffiliationDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private List<AcAuthorFosDto> fosList = new ArrayList<>();

    public AcAuthorDto(Author author, boolean loadFos) {
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

        if (loadFos && !CollectionUtils.isEmpty(author.getFosList())) {
            this.fosList = author.getFosList()
                    .stream()
                    .map(AcAuthorFosDto::new)
                    .sorted(Comparator.comparing(AcAuthorFosDto::getRank, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        }
    }

}
