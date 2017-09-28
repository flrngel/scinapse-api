package network.pluto.absolute.dto;

import lombok.Data;
import network.pluto.bibliotheca.models.Comment;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private long id;
    private MemberDto createdBy;
    private String comment;
    private LocalDateTime createdAt;

    public CommentDto() {
    }

    public CommentDto(Comment comment) {
        this.id = comment.getCommentId();
        this.createdBy = MemberDto.fromEntity(comment.getMember());
        this.comment = comment.getComment();
        this.createdAt = comment.getCreatedAt();
    }

    public Comment toEntity() {
        Comment comment = new Comment();
        comment.setMember(this.createdBy.toEntity());
        comment.setComment(this.comment);

        return comment;
    }
}
