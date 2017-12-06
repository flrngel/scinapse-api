package network.pluto.absolute.controller;

import network.pluto.absolute.dto.CommentDto;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.CommentService;
import network.pluto.bibliotheca.models.Comment;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @RequestMapping(value = "/comments/{commentId}", method = RequestMethod.PUT)
    public CommentDto updateComment(@ApiIgnore JwtUser user,
                                    @PathVariable long commentId,
                                    @RequestBody CommentDto commentDto) {
        Comment comment = commentService.find(commentId);
        if (comment == null) {
            throw new ResourceNotFoundException("Comment not found");
        }

        if (comment.getCreatedBy().getId() != user.getId()) {
            throw new AuthorizationServiceException("Members can update their own comment only");
        }

        Comment updated = commentService.updateComment(comment, commentDto.toEntity());
        return new CommentDto(updated);
    }

    @RequestMapping(value = "/comments/{commentId}", method = RequestMethod.DELETE)
    public Result deleteComment(@ApiIgnore JwtUser user,
                                @PathVariable long commentId) {
        Comment comment = commentService.find(commentId);
        if (comment == null) {
            throw new ResourceNotFoundException("Comment not found");
        }

        if (comment.getCreatedBy().getId() != user.getId()) {
            throw new AuthorizationServiceException("Deleting comment is only possible by its creator");
        }

        commentService.deleteComment(comment);
        return Result.success();
    }
}
