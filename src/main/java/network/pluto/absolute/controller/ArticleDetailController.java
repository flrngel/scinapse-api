package network.pluto.absolute.controller;

import network.pluto.absolute.dto.CommentDto;
import network.pluto.absolute.dto.EvaluationDto;
import network.pluto.absolute.security.jwt.JwtAuthenticationToken;
import network.pluto.absolute.service.CommentService;
import network.pluto.absolute.service.EvaluationService;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Comment;
import network.pluto.bibliotheca.models.Evaluation;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/article/{articleId}")
public class ArticleDetailController {

    private final MemberService memberService;
    private final EvaluationService evaluationService;
    private final CommentService commentService;

    @Autowired
    public ArticleDetailController(MemberService memberService,
                                   EvaluationService evaluationService,
                                   CommentService commentService) {
        this.memberService = memberService;
        this.evaluationService = evaluationService;
        this.commentService = commentService;
    }

    @RequestMapping(value = "/evaluations", method = RequestMethod.POST)
    public EvaluationDto createEvaluation(Principal principal,
                                          @PathVariable long articleId,
                                          @RequestBody EvaluationDto evaluationDto) {
        Evaluation evaluation = evaluationDto.toEntity();
        Member member = this.getMemberFromPrincipal(principal);
        evaluation.setMember(member);
        evaluation = this.evaluationService.saveEvaluation(articleId, evaluation);

        return new EvaluationDto(evaluation);
    }

    @RequestMapping(value = "/evaluations", method = RequestMethod.GET)
    public List<EvaluationDto> getEvaluations(@PathVariable long articleId) {
        List<Evaluation> evaluations = this.evaluationService.getEvaluations(articleId);

        return evaluations.stream().map(EvaluationDto::new).collect(Collectors.toList());
    }

    @RequestMapping(value = "/evaluations/{evaluationId}/comments", method = RequestMethod.POST)
    public EvaluationDto createComment(Principal principal,
                                       @PathVariable long evaluationId,
                                       @RequestBody CommentDto commentDto) {
        Comment comment = commentDto.toEntity();
        Member member = this.getMemberFromPrincipal(principal);
        comment.setMember(member);

        this.commentService.saveComment(evaluationId, comment);
        Evaluation evaluation = this.evaluationService.getEvaluation(evaluationId);

        return new EvaluationDto(evaluation);
    }

    @RequestMapping(value = "/evaluations/{evaluationId}/comments", method = RequestMethod.GET)
    public List<CommentDto> getComments(@PathVariable long evaluationId) {
        return this.commentService.getComments(evaluationId).stream().map(CommentDto::new).collect(Collectors.toList());
    }

    private Member getMemberFromPrincipal(Principal principal) {
        Member member = (Member) ((JwtAuthenticationToken) principal).getPrincipal();
        return this.memberService.findByEmail(member.getEmail());
    }
}
