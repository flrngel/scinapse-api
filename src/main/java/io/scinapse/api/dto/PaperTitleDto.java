package io.scinapse.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class PaperTitleDto {

    private long paperId;
    private String title;
    private Long citationCount;

    @JsonProperty("is_selected")
    private boolean selected = false;

}
