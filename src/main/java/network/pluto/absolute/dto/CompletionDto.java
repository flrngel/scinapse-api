package network.pluto.absolute.dto;

import lombok.EqualsAndHashCode;
import network.pluto.absolute.enums.CompletionType;

@EqualsAndHashCode(of = { "keyword", "type" })
public class CompletionDto {

    public String keyword;
    public CompletionType type;

    public CompletionDto(String keyword, CompletionType type) {
        this.keyword = keyword;
        this.type = type;
    }

}
