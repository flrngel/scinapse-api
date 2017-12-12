package network.pluto.absolute.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PaperSearchDto {

    private long id;

    private String title;

    @JsonProperty(value = "abstract")
    private String paperAbstract;

    private int year;
}
