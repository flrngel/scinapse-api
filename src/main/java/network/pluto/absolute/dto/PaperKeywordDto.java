package network.pluto.absolute.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.bibliotheca.models.PaperKeyword;

@NoArgsConstructor
@Getter
@Setter
public class PaperKeywordDto {

    private long paperId;
    private String keyword;

    public PaperKeywordDto(PaperKeyword paperKeyword) {
        this.paperId = paperKeyword.getPaper().getId();
        this.keyword = paperKeyword.getKeyword();
    }
}
