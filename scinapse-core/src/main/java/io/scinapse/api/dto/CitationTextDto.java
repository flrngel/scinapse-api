package io.scinapse.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.domain.enums.CitationFormat;

public class CitationTextDto {

    public CitationFormat format;

    @JsonProperty("citation_text")
    public String citationText;

}
