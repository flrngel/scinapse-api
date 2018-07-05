package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.absolute.models.Comment;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class CommentDto {

    @ApiModelProperty(readOnly = true)
    private long id;

    @ApiModelProperty(required = true)
    @NotNull
    private String comment;

    @ApiModelProperty(required = true)
    private Long paperId;

    @ApiModelProperty(readOnly = true)
    private MemberDto createdBy;

    @ApiModelProperty(readOnly = true)
    private LocalDateTime createdAt;

    public CommentDto(Comment comment) {
        this.id = comment.getId();
        this.comment = comment.getComment();
        this.paperId = comment.getPaperId();
        this.createdBy = new MemberDto(comment.getCreatedBy());
        this.createdAt = comment.getCreatedAt();
    }

    public Comment toEntity() {
        Comment comment = new Comment();
        comment.setComment(this.comment);
        return comment;
    }

}
