package network.pluto.absolute.enums;

import lombok.Getter;

@Getter
public enum CitationFormat {
    BIBTEX("application/x-bibtex"),
    RIS("application/x-research-info-systems"),
    APA("text/x-bibliography; style=apa"),
    IEEE("text/x-bibliography; style=ieee"),
    HARVARD("text/x-bibliography; style=harvard3"),
    MLA("text/x-bibliography; style=mla"),
    VANCOUVER("text/x-bibliography; style=vancouver"),
    CHICAGO("text/x-bibliography; style=chicago-note-bibliography");

    private String accept;

    CitationFormat(String accept) {
        this.accept = accept;
    }
}
