package io.scinapse.api.dto.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Preconditions;
import io.scinapse.api.academic.dto.AcPaperDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class PaperItemDto {

    @JsonUnwrapped
    private AcPaperDto origin;

    @JsonProperty("is_layered")
    private boolean layered = false;

    private String titleHighlighted;
    private String abstractHighlighted;

    private List<PaperAuthorDto> authors;

    @JsonUnwrapped
    private Object additional;

    public PaperItemDto(AcPaperDto dto) {
        Preconditions.checkNotNull(dto);

        this.origin = dto;

        this.titleHighlighted = dto.getTitle();
        this.abstractHighlighted = dto.getPaperAbstract();

        this.authors = dto.getAuthors()
                .stream()
                .map(PaperAuthorDto::new)
                .collect(Collectors.toList());
    }

    @RequiredArgsConstructor
    public static class ForAuthorAdditional {
        @JsonProperty("is_author_included")
        private final boolean included;
    }

}
