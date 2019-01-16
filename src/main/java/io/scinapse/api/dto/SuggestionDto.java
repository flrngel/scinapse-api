package io.scinapse.api.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
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

    public SuggestionDto(String keyword, String suggestion, String highlighted) {
        this.originalQuery = keyword;
        this.suggestQuery = suggestion;
        this.highlighted = highlighted;
    }

    @JsonGetter
    public String getKeyword() {
        return this.originalQuery;
    }

    @JsonGetter
    public String getSuggestion() {
        return this.suggestQuery;
    }

}
