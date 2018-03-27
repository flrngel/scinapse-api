package network.pluto.absolute.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import network.pluto.absolute.enums.CitationFormat;

public class CitationTextDto {

    public CitationFormat format;

    @JsonProperty("citation_text")
    public String citationText;

}
