package io.scinapse.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaperTitleDto {

    private long paperId;
    private String title;

    @JsonProperty("is_selected")
    private boolean selected;

}
