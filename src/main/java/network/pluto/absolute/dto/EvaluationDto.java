package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import network.pluto.bibliotheca.models.Evaluation;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Data
public class EvaluationDto {

    @ApiModelProperty(readOnly = true)
    private long id;

    @ApiModelProperty(readOnly = true)
    private long articleId;

    private int point;

    @ApiModelProperty(required = true)
    @NotNull
    private String evaluation;

    @ApiModelProperty(readOnly = true)
    private int vote;

    @ApiModelProperty(readOnly = true)
    private boolean voted;

    @ApiModelProperty(readOnly = true)
    private List<CommentDto> comments;

    @ApiModelProperty(readOnly = true)
    private MemberDto createdBy;

    @ApiModelProperty(readOnly = true)
    private LocalDateTime createdAt;

    public EvaluationDto(Evaluation evaluation, boolean voted) {
        this.id = evaluation.getEvaluationId();
        this.articleId = evaluation.getArticle().getArticleId();
        this.point = evaluation.getPoint();
        this.evaluation = evaluation.getEvaluation();
        this.vote = evaluation.getVote();
        this.comments = evaluation.getComments().stream().map(CommentDto::new).collect(Collectors.toList());
        this.createdBy = new MemberDto(evaluation.getCreatedBy());
        this.createdAt = evaluation.getCreatedAt();

        this.voted = voted;
    }

    public EvaluationDto(Evaluation evaluation) {
        this(evaluation, false);
    }

    public Evaluation toEntity() {
        Evaluation evaluation = new Evaluation();
        evaluation.setPoint(this.point);
        evaluation.setEvaluation(this.evaluation);
        return evaluation;
    }
}
