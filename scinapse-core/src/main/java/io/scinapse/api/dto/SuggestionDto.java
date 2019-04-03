package io.scinapse.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class SuggestionDto {

    private String originalQuery;
    private String suggestQuery;
    private String highlighted;

    public SuggestionDto(String originalQuery, String suggestQuery, String highlighted) {
        this.originalQuery = originalQuery;
        this.suggestQuery = suggestQuery;
        this.highlighted = highlighted;
    }

}
