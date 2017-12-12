package network.pluto.absolute.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.bibliotheca.models.PaperUrl;

@NoArgsConstructor
@Getter
@Setter
public class PaperUrlDto {

    private long id;
    private long paperId;
    private String url;

    public PaperUrlDto(PaperUrl paperUrl) {
        this.id = paperUrl.getId();
        this.paperId = paperUrl.getPaper().getId();
        this.url = paperUrl.getUrl();
    }
}
