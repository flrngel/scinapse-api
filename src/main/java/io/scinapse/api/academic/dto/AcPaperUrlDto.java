package io.scinapse.api.academic.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.data.academic.PaperUrl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class AcPaperUrlDto {

    private long id;
    private String url;

    public AcPaperUrlDto(PaperUrl paperUrl) {
        this.id = paperUrl.getId();
        this.url = paperUrl.getSourceUrl();
    }

}
