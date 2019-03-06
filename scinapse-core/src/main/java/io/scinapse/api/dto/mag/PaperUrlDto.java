package io.scinapse.api.dto.mag;

import com.fasterxml.jackson.annotation.JsonGetter;
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
    private boolean isPdf;

    public PaperUrlDto(PaperUrl paperUrl) {
        this.paperId = paperUrl.getPaper().getId();
        this.id = paperUrl.getId();
        this.url = paperUrl.getSourceUrl();
        this.isPdf = paperUrl.isPdf();
    }

    @JsonGetter("is_pdf")
    public boolean isPdf() {
        return this.isPdf;
    }
}
