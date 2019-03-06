package io.scinapse.api.dto.mag;

import io.scinapse.domain.data.academic.PaperUrl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PaperUrlDto {

    private long paperId;
    private long id;
    private String url;

    public PaperUrlDto(PaperUrl paperUrl) {
        this.paperId = paperUrl.getPaper().getId();
        this.id = paperUrl.getId();
        this.url = paperUrl.getSourceUrl();
    }

}
