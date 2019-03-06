package io.scinapse.api.dto.mag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorSearchPaperDto {

    @JsonUnwrapped
    private PaperDto dto;

    @JsonProperty("is_author_included")
    private boolean included;

    public AuthorSearchPaperDto(PaperDto dto, boolean included) {
        this.dto = dto;
        this.included = included;
    }

}
