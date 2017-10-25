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
    private long articleId;

    @ApiModelProperty(readOnly = true)
    private int vote;

    @ApiModelProperty(readOnly = true)
    private boolean voted;

    @ApiModelProperty(readOnly = true)
    private List<CommentDto> comments;

    @ApiModelProperty(readOnly = true)
    private int commentSize;

    @ApiModelProperty(readOnly = true)
    private MemberDto createdBy;

    @ApiModelProperty(readOnly = true)
    private LocalDateTime createdAt;

    public EvaluationDto(Evaluation evaluation, boolean voted) {
        this.id = evaluation.getEvaluationId();
        this.articleId = evaluation.getArticle().getArticleId();
        this.vote = evaluation.getVote();
        this.commentSize = evaluation.getCommentSize();
        this.createdBy = new MemberDto(evaluation.getCreatedBy());
        this.createdAt = evaluation.getCreatedAt();

        this.voted = voted;

        if (evaluation.getPoint() != null) {
            this.point = new EvaluationPointDto(evaluation.getPoint());
        }

        if (evaluation.getComments() != null) {
            this.comments = evaluation.getComments().stream().map(CommentDto::new).collect(Collectors.toList());
        }
    }

    public EvaluationDto(Evaluation evaluation) {
        this(evaluation, false);
    }

    public Evaluation toEntity() {
        Evaluation evaluation = new Evaluation();

        if (this.point != null) {
            evaluation.setPoint(this.point.toEntity());
        }

        return evaluation;
    }
}
