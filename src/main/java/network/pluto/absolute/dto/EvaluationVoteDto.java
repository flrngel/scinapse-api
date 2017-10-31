package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class EvaluationVoteDto {

    @ApiModelProperty(readOnly = true)
    private long evaluationId;

    @ApiModelProperty(readOnly = true)
    private long memberId;

    @ApiModelProperty(readOnly = true)
    private int vote = 0;

    @ApiModelProperty(readOnly = true)
    private boolean voted = false;
}
