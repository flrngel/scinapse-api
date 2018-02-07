package network.pluto.absolute.dto.cognitive;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalcHistogramResponseDto {

    @JsonProperty("expr")
    private String expression;

    @JsonProperty("num_entities")
    private long totalElements;

}
