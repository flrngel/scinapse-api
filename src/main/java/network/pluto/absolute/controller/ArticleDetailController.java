package network.pluto.absolute.controller;

import network.pluto.absolute.dto.CommentDto;
import network.pluto.absolute.dto.EvaluationDto;
import network.pluto.absolute.dto.EvaluationVoteDto;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.CommentService;
import network.pluto.absolute.service.EvaluationService;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Comment;
import network.pluto.bibliotheca.models.Evaluation;
import network.pluto.bibliotheca.models.EvaluationVote;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/articles/{articleId}")
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
    public EvaluationDto createEvaluation(JwtUser user,
                                          @PathVariable long articleId,
                                          @RequestBody @Valid EvaluationDto evaluationDto) {
        Member member = memberService.getMember(user.getId());

        Evaluation evaluation = evaluationDto.toEntity();
        evaluation.setCreatedBy(member);
        evaluation = this.evaluationService.saveEvaluation(articleId, evaluation);

        // increase member reputation
        memberService.increaseReputation(member.getMemberId(), 5);

        return new EvaluationDto(evaluation);
    }

    @RequestMapping(value = "/evaluations", method = RequestMethod.GET)
    public List<EvaluationDto> getEvaluations(@PathVariable long articleId) {
        List<Evaluation> evaluations = this.evaluationService.getEvaluations(articleId);

        return evaluations.stream().map(EvaluationDto::new).collect(Collectors.toList());
    }

    @RequestMapping(value = "/evaluations/{evaluationId}/vote", method = RequestMethod.POST)
    public EvaluationDto pressVote(JwtUser user,
                                   @PathVariable long evaluationId) {
        Member member = memberService.getMember(user.getId());
        Evaluation evaluation = this.evaluationService.increaseVote(evaluationId, member);

        // increase member reputation
        memberService.increaseReputation(evaluation.getCreatedBy().getMemberId(), 1);

        return new EvaluationDto(evaluation);
    }

    @RequestMapping(value = "/evaluations/{evaluationId}/vote", method = RequestMethod.GET)
    public EvaluationVoteDto checkVote(JwtUser user,
                                       @PathVariable long evaluationId) {
        EvaluationVoteDto dto = new EvaluationVoteDto();
        dto.setEvaluationId(evaluationId);
        dto.setMemberId(user.getId());

        EvaluationVote evaluationVote = this.evaluationService.checkVote(user.getId(), evaluationId);
        if (evaluationVote != null) {
            dto.setVote(true);
        }
        return dto;
    }

    @RequestMapping(value = "/evaluations/{evaluationId}/comments", method = RequestMethod.POST)
    public EvaluationDto createComment(JwtUser user,
                                       @PathVariable long evaluationId,
                                       @RequestBody @Valid CommentDto commentDto) {
        Member member = memberService.getMember(user.getId());

        Comment comment = commentDto.toEntity();
        comment.setCreatedBy(member);

        this.commentService.saveComment(evaluationId, comment);
        Evaluation evaluation = this.evaluationService.getEvaluation(evaluationId);

        return new EvaluationDto(evaluation);
    }

    @RequestMapping(value = "/evaluations/{evaluationId}/comments", method = RequestMethod.GET)
    public List<CommentDto> getComments(@PathVariable long evaluationId) {
        return this.commentService.getComments(evaluationId).stream().map(CommentDto::new).collect(Collectors.toList());
    }
}
