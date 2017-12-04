package network.pluto.absolute.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PaperSearchDto {

    @JsonProperty("id")
    private String magId;

    private String title;

    private int year;

    @JsonProperty("n_citation")
    private Integer nCitation;

    @JsonProperty("abstract")
    private String paperAbstract;

    private String urls;

    private String lang;

    private String doi;

    @JsonProperty("doc_type")
    private String docType;

    @JsonProperty("journal_name")
    private String journalName;

    @JsonProperty("journal_id")
    private Integer journalId;

    private String publisher;

    private Integer volume;

    private Integer issue;

    @JsonProperty("page_start")
    private String pageStart;

    @JsonProperty("page_end")
    private String pageEnd;
}
