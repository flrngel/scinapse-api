package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import network.pluto.bibliotheca.models.Evaluation;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Data
public class EvaluationDto {

    @ApiModelProperty(readOnly = true)
    private long id;

    @ApiModelProperty(required = true)
    @NotNull
    @Valid
    private EvaluationPointDto point;

    @ApiModelProperty(readOnly = true)
    private int vote;

    @ApiModelProperty(readOnly = true)
    private List<CommentDto> comments;

    @ApiModelProperty(readOnly = true)
    private MemberDto createdBy;

    @ApiModelProperty(readOnly = true)
    private LocalDateTime createdAt;

    public EvaluationDto(Evaluation evaluation) {
        this.id = evaluation.getEvaluationId();
        this.vote = evaluation.getVote();
        this.comments = evaluation.getComments().stream().map(CommentDto::new).collect(Collectors.toList());
        this.createdBy = new MemberDto(evaluation.getCreatedBy());
        this.createdAt = evaluation.getCreatedAt();

        if (evaluation.getPoint() != null) {
            this.point = new EvaluationPointDto(evaluation.getPoint());
        }
    }

    public Evaluation toEntity() {
        Evaluation evaluation = new Evaluation();

        if (this.point != null) {
            evaluation.setPoint(point.toEntity());
        }

        return evaluation;
    }
}
