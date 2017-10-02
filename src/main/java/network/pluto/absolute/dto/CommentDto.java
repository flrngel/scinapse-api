package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import network.pluto.bibliotheca.models.Comment;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class CommentDto {

    @ApiModelProperty(readOnly = true)
    private long id;

    @ApiModelProperty(readOnly = true)
    private MemberDto createdBy;

    @ApiModelProperty(required = true)
    private String comment;

    @ApiModelProperty(readOnly = true)
    private LocalDateTime createdAt;

    public CommentDto(Comment comment) {
        this.id = comment.getCommentId();
        this.createdBy = new MemberDto(comment.getMember());
        this.comment = comment.getComment();
        this.createdAt = comment.getCreatedAt();
    }

    public Comment toEntity() {
        Comment comment = new Comment();
        comment.setCommentId(this.id);
        if (this.createdBy != null) {
            comment.setMember(this.createdBy.toEntity());
        }
        comment.setComment(this.comment);

        return comment;
    }
}
