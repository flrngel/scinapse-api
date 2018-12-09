package io.scinapse.api.dto.mag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.scinapse.api.data.scinapse.model.author.AuthorLayerPaper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorPaperDto {

    @JsonUnwrapped
    private PaperDto dto;

    private AuthorLayerPaper.PaperStatus status;

    @JsonProperty("is_selected")
    private boolean selected;

    @JsonProperty("is_representative")
    private boolean representative;

    public AuthorPaperDto(PaperDto dto, AuthorLayerPaper.PaperStatus status, boolean representative) {
        this.dto = dto;
        this.status = status;
        this.representative = representative;
        this.selected = representative;
    }

}
