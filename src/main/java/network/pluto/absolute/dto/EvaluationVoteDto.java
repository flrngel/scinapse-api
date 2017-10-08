package network.pluto.absolute.dto;

import lombok.Data;

@Data
public class EvaluationVoteDto {
    private long evaluationId;
    private long memberId;
    private boolean vote = false;
}
