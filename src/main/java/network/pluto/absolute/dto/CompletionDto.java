package network.pluto.absolute.dto;

import network.pluto.absolute.enums.CompletionType;

public class CompletionDto {

    public String keyword;
    public CompletionType type;

    public CompletionDto(String keyword, CompletionType type) {
        this.keyword = keyword;
        this.type = type;
    }

}
