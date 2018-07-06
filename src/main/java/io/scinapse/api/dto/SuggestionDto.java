package io.scinapse.api.dto;

public class SuggestionDto {

    public String keyword;
    public String suggestion;
    public String highlighted;

    public SuggestionDto(String keyword, String suggestion, String highlighted) {
        this.keyword = keyword;
        this.suggestion = suggestion;
        this.highlighted = highlighted;
    }

}
