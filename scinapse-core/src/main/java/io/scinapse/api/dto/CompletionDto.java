package io.scinapse.api.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.scinapse.domain.enums.CompletionType;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(of = { "keyword", "type" })
public class CompletionDto {

    public String keyword;
    public CompletionType type;

    @JsonIgnore
    public Map<Object, Object> additionalInfo = new HashMap<>();

    @JsonAnyGetter
    public Map<Object, Object> getAdditionalInfo() {
        return this.additionalInfo;
    }

    public CompletionDto(String keyword, CompletionType type) {
        this.keyword = keyword;
        this.type = type;
    }

}
