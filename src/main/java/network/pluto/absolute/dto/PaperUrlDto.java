package network.pluto.absolute.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.absolute.models.mag.PaperUrl;

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
