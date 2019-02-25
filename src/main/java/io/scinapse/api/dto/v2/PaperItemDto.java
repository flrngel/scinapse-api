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

import java.util.ArrayList;
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

    @JsonProperty("abstract")
    private String abstractOverride = null;

    private String titleHighlighted;
    private String abstractHighlighted;

    private List<PaperAuthorDto> authors;

    private Relation relation;

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

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Getter
    @Setter
    public static class Relation {
        private List<SavedInCollection> savedInCollections = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class SavedInCollection {
        private long id;
        private String title;
    }

    @RequiredArgsConstructor
    public static class ForAuthorAdditional {
        @JsonProperty("is_author_included")
        private final boolean included;
    }

}
