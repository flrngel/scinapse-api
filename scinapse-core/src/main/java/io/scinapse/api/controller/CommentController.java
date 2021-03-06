package io.scinapse.api.controller;

import io.scinapse.domain.data.academic.model.Paper;
import io.scinapse.domain.data.scinapse.model.Comment;
import io.scinapse.domain.data.scinapse.model.Member;
import io.scinapse.api.dto.CommentDto;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.security.jwt.JwtUser;
import io.scinapse.api.service.CommentService;
import io.scinapse.api.service.MemberService;
import io.scinapse.api.service.mag.PaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final MemberService memberService;
    private final PaperService paperService;

    @Transactional
    @RequestMapping(value = "/comments", method = RequestMethod.POST)
    public CommentDto createComment(@ApiIgnore JwtUser user,
                                    @RequestBody @Valid CommentDto commentDto) {
        Paper paper = paperService.find(commentDto.getPaperId());
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found: " + commentDto.getPaperId());
        }

        Member member = memberService.getMember(user.getId());

        Comment comment = commentDto.toEntity();
        comment.setCreatedBy(member);

        comment = commentService.saveComment(paper.getId(), comment);
        return new CommentDto(comment);
    }

    @RequestMapping(value = "/comments", method = RequestMethod.GET)
    public Page<CommentDto> findComments(@RequestParam Long paperId,
                                         PageRequest pageRequest) {
        Paper paper = paperService.find(paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found: " + paperId);
        }

        return commentService.findByPaperId(paperId, pageRequest).map(CommentDto::new);
    }

    @Transactional
    @RequestMapping(value = "/comments/{commentId}", method = RequestMethod.PUT)
    public CommentDto updateComment(@ApiIgnore JwtUser user,
                                    @PathVariable long commentId,
                                    @RequestBody CommentDto commentDto) {
        Comment comment = commentService.find(commentId);
        if (comment == null) {
            throw new ResourceNotFoundException("Comment not found: " + commentId);
        }

        if (comment.getCreatedBy().getId() != user.getId()) {
            throw new AuthorizationServiceException("Members can update their own comment only");
        }

        Comment updated = commentService.updateComment(comment, commentDto.toEntity());
        return new CommentDto(updated);
    }

    @Transactional
    @RequestMapping(value = "/comments/{commentId}", method = RequestMethod.DELETE)
    public Result deleteComment(@ApiIgnore JwtUser user,
                                @PathVariable long commentId) {
        Comment comment = commentService.find(commentId);
        if (comment == null) {
            throw new ResourceNotFoundException("Comment not found: " + commentId);
        }

        if (comment.getCreatedBy().getId() != user.getId()) {
            throw new AuthorizationServiceException("Deleting comment is only possible by its creator");
        }

        commentService.deleteComment(comment);
        return Result.success();
    }

}
