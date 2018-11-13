package io.scinapse.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaperImageDto {
    private long paperId;
    private String imageUrl;
}
