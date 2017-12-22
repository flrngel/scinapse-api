package network.pluto.absolute.controller;

import network.pluto.absolute.dto.CommentDto;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.facade.PaperFacade;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.CommentService;
import network.pluto.absolute.service.MemberService;
import network.pluto.absolute.service.PaperService;
import network.pluto.absolute.util.Query;
import network.pluto.absolute.util.QueryParser;
import network.pluto.bibliotheca.models.Comment;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.Paper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

@RestController
public class PaperController {

    private final PaperService paperService;
    private final PaperFacade paperFacade;
    private final CommentService commentService;
    private final MemberService memberService;

    @Autowired
    public PaperController(PaperService paperService,
                           PaperFacade paperFacade,
                           CommentService commentService,
                           MemberService memberService) {
        this.paperService = paperService;
        this.paperFacade = paperFacade;
        this.commentService = commentService;
        this.memberService = memberService;
    }

    @RequestMapping(value = "/papers/{paperId}", method = RequestMethod.GET)
    public PaperDto find(@PathVariable long paperId) {
        return paperFacade.find(paperId);
    }

    @RequestMapping(value = "/papers", method = RequestMethod.GET)
    public Page<PaperDto> search(@RequestParam("query") String queryStr, @PageableDefault Pageable pageable) {
        if (!StringUtils.hasText(queryStr)) {
            throw new BadRequestException("Invalid query: query not exists");
        }

        Query query = QueryParser.parse(queryStr);
        if (!query.isValid()) {
            throw new BadRequestException("Invalid query: too short query text");
        }

        return paperFacade.search(query, pageable);
    }

    @RequestMapping(value = "/papers/{paperId}/comments", method = RequestMethod.POST)
    public CommentDto createComment(@ApiIgnore JwtUser user,
                                    @PathVariable long paperId,
                                    @RequestBody @Valid CommentDto commentDto) {
        Paper paper = paperService.find(paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found");
        }

        Member member = memberService.getMember(user.getId());

        Comment comment = commentDto.toEntity();
        comment.setCreatedBy(member);

        comment = commentService.saveComment(paper, comment);
        return new CommentDto(comment);
    }

    @RequestMapping(value = "/papers/{paperId}/comments", method = RequestMethod.GET)
    public Page<CommentDto> findComments(@PathVariable long paperId,
                                         @PageableDefault Pageable pageable) {
        Paper paper = paperService.find(paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found");
        }

        return commentService.findByPaper(paper, pageable).map(CommentDto::new);
    }

    @RequestMapping(value = "/papers/{paperId}/comments/{commentId}", method = RequestMethod.PUT)
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

    @RequestMapping(value = "/papers/{paperId}/comments/{commentId}", method = RequestMethod.DELETE)
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

    @RequestMapping(value = "/papers/{paperId}/references", method = RequestMethod.GET)
    public Page<PaperDto> paperReferences(@PathVariable long paperId, @PageableDefault Pageable pageable) {
        return paperFacade.findReferences(paperId, pageable);
    }

    @RequestMapping(value = "/papers/{paperId}/cited", method = RequestMethod.GET)
    public Page<PaperDto> paperCited(@PathVariable long paperId, @PageableDefault Pageable pageable) {
        return paperFacade.findCited(paperId, pageable);
    }
}
