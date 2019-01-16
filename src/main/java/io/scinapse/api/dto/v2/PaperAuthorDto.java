package io.scinapse.api.dto.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.academic.dto.AcPaperAuthorDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class PaperAuthorDto {

    @JsonUnwrapped
    private AcPaperAuthorDto origin;

    @JsonProperty("is_layered")
    private boolean layered = false;

    private String name;
    private Integer hIndex;
    private long paperCount;
    private long citationCount;

    private String profileImageUrl;

    public PaperAuthorDto(AcPaperAuthorDto dto) {
        this.origin = dto;

        this.name = dto.getName();
        this.hIndex = dto.getHIndex();
        this.paperCount = dto.getPaperCount();
        this.citationCount = dto.getCitationCount();
    }

}
