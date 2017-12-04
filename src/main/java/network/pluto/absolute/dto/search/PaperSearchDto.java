package network.pluto.absolute.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PaperSearchDto {

    private long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String magId;

    private String title;

    private int year;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer citation;

    @JsonProperty(value = "abstract", access = JsonProperty.Access.READ_ONLY)
    private String paperAbstract;

    private String lang;

    private String doi;

    private String publisher;

    private String venue;

    private Integer volume;

    private Integer issue;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String pageStart;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String pageEnd;

    @JsonSetter("mag_id")
    public void setMagId(String magId) {
        this.magId = magId;
    }

    @JsonSetter("n_citation")
    public void setCitation(Integer citation) {
        this.citation = citation;
    }

    @JsonSetter("abstract")
    public void setPaperAbstract(String paperAbstract) {
        this.paperAbstract = paperAbstract;
    }

    @JsonSetter("page_start")
    public void setPageStart(String pageStart) {
        this.pageStart = pageStart;
    }

    @JsonSetter("page_end")
    public void setPageEnd(String pageEnd) {
        this.pageEnd = pageEnd;
    }
}
