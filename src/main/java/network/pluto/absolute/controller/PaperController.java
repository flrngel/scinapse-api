package network.pluto.absolute.controller;

import network.pluto.absolute.dto.CommentDto;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.facade.PaperFacade;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.CommentService;
import network.pluto.absolute.service.MemberService;
import network.pluto.absolute.service.PaperService;
import network.pluto.bibliotheca.models.Comment;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.Paper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public PaperController(PaperService paperService, PaperFacade paperFacade, CommentService commentService, MemberService memberService) {
        this.paperService = paperService;
        this.paperFacade = paperFacade;
        this.commentService = commentService;
        this.memberService = memberService;
    }

    @RequestMapping(value = "/papers/{paperId}", method = RequestMethod.GET)
    public PaperDto find(@PathVariable long paperId) {
        return paperFacade.find(paperId);
    }

    @RequestMapping(value = "/papers/search", method = RequestMethod.GET)
    public Page<PaperDto> search(@RequestParam String text, @PageableDefault Pageable pageable) {
        return paperFacade.search(text, pageable);
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
}
