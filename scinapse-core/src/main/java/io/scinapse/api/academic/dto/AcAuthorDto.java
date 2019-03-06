package io.scinapse.api.academic.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.domain.data.academic.Author;
import io.scinapse.domain.data.academic.AuthorHIndex;
import io.scinapse.domain.data.academic.AuthorTopPaper;
import io.scinapse.api.dto.mag.AffiliationDto;
import lombok.Builder;
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
    private List<AcPaperDto> topPapers = new ArrayList<>();

    public AcAuthorDto(Author author, DetailSelector selector) {
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

        if (selector.loadFos && !CollectionUtils.isEmpty(author.getFosList())) {
            this.fosList = author.getFosList()
                    .stream()
                    .map(AcAuthorFosDto::new)
                    .sorted(Comparator.comparing(AcAuthorFosDto::getRank, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        }

        if (selector.loadTopPaper && !CollectionUtils.isEmpty(author.getTopPapers())) {
            this.topPapers = author.getTopPapers()
                    .stream()
                    .map(AuthorTopPaper::getPaper)
                    .map(paper -> new AcPaperDto(paper, AcPaperDto.DetailSelector.none()))
                    .sorted(Comparator.comparing(AcPaperDto::getCitedCount).reversed())
                    .collect(Collectors.toList());
        }
    }

    @Builder
    public static class DetailSelector {
        private boolean loadFos;
        private boolean loadTopPaper;

        public static DetailSelector none() {
            return builder().build();
        }

        public static DetailSelector full() {
            return builder()
                    .loadFos(true)
                    .loadTopPaper(true)
                    .build();
        }
    }

}
