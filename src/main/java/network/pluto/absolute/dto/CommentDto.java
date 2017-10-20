package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import network.pluto.bibliotheca.models.Comment;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class CommentDto {

    @ApiModelProperty(readOnly = true)
    private long id;

    @ApiModelProperty(required = true)
    @NotNull
    private String comment;

    @ApiModelProperty(readOnly = true)
    private long evaluationId;

    @ApiModelProperty(readOnly = true)
    private MemberDto createdBy;

    @ApiModelProperty(readOnly = true)
    private LocalDateTime createdAt;

    public CommentDto(Comment comment) {
        this.id = comment.getCommentId();
        this.comment = comment.getComment();
        this.evaluationId = comment.getEvaluation().getEvaluationId();
        this.createdBy = new MemberDto(comment.getCreatedBy());
        this.createdAt = comment.getCreatedAt();
    }

    public Comment toEntity() {
        Comment comment = new Comment();
        comment.setComment(this.comment);
        return comment;
    }
}
