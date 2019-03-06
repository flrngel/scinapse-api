package io.scinapse.api.dto;

import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;
import java.util.List;

public class CompletionResponseDto {

    private List<String> completions = new ArrayList<>();

    public List<String> getCompletions() {
        return completions;
    }

    @JsonSetter("l")
    public void setCompletions(List<String> completions) {
        this.completions = completions;
    }

}
