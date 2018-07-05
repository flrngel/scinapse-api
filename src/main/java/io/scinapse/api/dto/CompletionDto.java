package io.scinapse.api.dto;

import io.scinapse.api.enums.CompletionType;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = { "keyword", "type" })
public class CompletionDto {

    public String keyword;
    public CompletionType type;

    public CompletionDto(String keyword, CompletionType type) {
        this.keyword = keyword;
        this.type = type;
    }

}
