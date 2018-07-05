package network.pluto.absolute.controller;

import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.CommentDto;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.model.Comment;
import network.pluto.absolute.model.Member;
import network.pluto.absolute.model.mag.Paper;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.CommentService;
import network.pluto.absolute.service.MemberService;
import network.pluto.absolute.service.mag.PaperService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final MemberService memberService;
    private final PaperService paperService;

    @RequestMapping(value = "/comments", method = RequestMethod.POST)
    public CommentDto createComment(@ApiIgnore JwtUser user,
                                    @RequestBody @Valid CommentDto commentDto) {
        Paper paper = paperService.find(commentDto.getPaperId());
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found");
        }

        Member member = memberService.getMember(user.getId());

        Comment comment = commentDto.toEntity();
        comment.setCreatedBy(member);

        comment = commentService.saveComment(paper.getId(), comment);
        return new CommentDto(comment);
    }

    @RequestMapping(value = "/comments", method = RequestMethod.GET)
    public Page<CommentDto> findComments(@RequestParam Long paperId,
                                         @PageableDefault Pageable pageable) {
        Paper paper = paperService.find(paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found");
        }

        return commentService.findByPaperId(paperId, pageable).map(CommentDto::new);
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
