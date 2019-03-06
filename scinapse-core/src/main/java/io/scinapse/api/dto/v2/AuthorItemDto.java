package io.scinapse.api.dto.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.academic.dto.AcAuthorDto;
import io.scinapse.api.academic.dto.AcAuthorFosDto;
import io.scinapse.api.academic.dto.AcPaperDto;
import io.scinapse.domain.data.scinapse.model.author.AuthorLayerPaper;
import io.scinapse.api.dto.mag.AffiliationDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class AuthorItemDto {

    @JsonUnwrapped
    private AcAuthorDto origin;

    @JsonProperty("is_layered")
    private boolean layered = false;

    private String name;
    private Integer hIndex;
    private long paperCount;
    private long citationCount;
    private AffiliationDto lastKnownAffiliation;

    private List<AcAuthorFosDto> fosList = new ArrayList<>();
    private List<AcPaperDto> topPapers = new ArrayList<>();
    private List<AuthorPaperDto> representativePapers = new ArrayList<>();

    private String profileImageUrl;

    public AuthorItemDto(AcAuthorDto dto) {
        this.origin = dto;

        this.name = dto.getName();
        this.hIndex = dto.getHIndex();
        this.paperCount = dto.getPaperCount();
        this.citationCount = dto.getCitationCount();
        this.lastKnownAffiliation = dto.getLastKnownAffiliation();

        this.fosList = dto.getFosList();
        this.topPapers = dto.getTopPapers();
    }

    public void setRepresentativePapers(List<AuthorLayerPaper> layerPapers) {
        this.representativePapers = layerPapers
                .stream()
                .map(lp -> {
                    AuthorPaperDto paperDto = new AuthorPaperDto();
                    paperDto.id = lp.getId().getPaperId();
                    paperDto.title = lp.getTitle();
                    paperDto.year = lp.getYear();
                    paperDto.citationCount = lp.getCitationCount();
                    return paperDto;
                })
                .collect(Collectors.toList());
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Getter
    @Setter
    @NoArgsConstructor
    public class AuthorPaperDto {
        private long id;
        private String title;
        private Integer year;
        private Long citationCount;
    }

}
