package network.pluto.absolute.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.bibliotheca.models.PaperUrl;

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
        this.url = paperUrl.getUrl();
    }

    public PaperUrlDto(network.pluto.bibliotheca.models.mag.PaperUrl paperUrl) {
        this.paperId = paperUrl.getPaper().getId();
        this.id = paperUrl.getId();
        this.url = paperUrl.getSourceUrl();
    }

}
