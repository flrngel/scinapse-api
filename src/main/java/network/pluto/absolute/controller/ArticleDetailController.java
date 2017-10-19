package network.pluto.absolute.controller;

import network.pluto.absolute.dto.CommentDto;
import network.pluto.absolute.dto.EvaluationDto;
import network.pluto.absolute.dto.EvaluationVoteDto;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.ArticleService;
import network.pluto.absolute.service.CommentService;
import network.pluto.absolute.service.EvaluationService;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Article;
import network.pluto.bibliotheca.models.Comment;
import network.pluto.bibliotheca.models.Evaluation;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/articles/{articleId}")
public class ArticleDetailController {

    private final MemberService memberService;
    private final ArticleService articleService;
    private final EvaluationService evaluationService;
    private final CommentService commentService;

    @Autowired
    public ArticleDetailController(MemberService memberService,
                                   ArticleService articleService,
                                   EvaluationService evaluationService,
                                   CommentService commentService) {
        this.memberService = memberService;
        this.articleService = articleService;
        this.evaluationService = evaluationService;
        this.commentService = commentService;
    }

    @RequestMapping(value = "/evaluations", method = RequestMethod.POST)
    public EvaluationDto createEvaluation(@ApiIgnore JwtUser user,
                                          @PathVariable long articleId,
                                          @RequestBody @Valid EvaluationDto evaluationDto) {
        Article article = articleService.findArticle(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("Article not found");
        }

        Member member = memberService.getMember(user.getId());

        Evaluation evaluation = evaluationDto.toEntity();
        evaluation.setCreatedBy(member);

        evaluation = evaluationService.saveEvaluation(article, evaluation);

        // increase member reputation
        memberService.increaseReputation(member, 5);

        return new EvaluationDto(evaluation);
    }

    @RequestMapping(value = "/evaluations", method = RequestMethod.GET)
    public Page<EvaluationDto> findEvaluations(@ApiIgnore JwtUser user,
                                               @PathVariable long articleId,
                                               @PageableDefault Pageable pageable) {
        Article article = articleService.findArticle(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("Article not found");
        }

        Page<Evaluation> evaluations = evaluationService.findByArticle(article, pageable);
        Page<EvaluationDto> dtoList = evaluations.map(EvaluationDto::new);

        if (user != null) {
            Member member = memberService.getMember(user.getId());

            Map<Long, Boolean> votedMap = evaluationService.checkVoted(member, evaluations.getContent());
            dtoList.forEach(e -> {
                if (votedMap.get(e.getId())) {
                    e.setVoted(true);
                }
            });
        }

        return dtoList;
    }

    @RequestMapping(value = "/evaluations/{evaluationId}/vote", method = RequestMethod.POST)
    public Result pressVote(@ApiIgnore JwtUser user,
                            @PathVariable long evaluationId) {
        Evaluation evaluation = evaluationService.findEvaluation(evaluationId);
        if (evaluation == null) {
            throw new ResourceNotFoundException("Evaluation not found");
        }

        Member member = memberService.getMember(user.getId());

        // increase evaluation vote number
        evaluationService.increaseVote(evaluation, member);

        // increase member reputation
        memberService.increaseReputation(member, 1);

        return Result.success();
    }

    @RequestMapping(value = "/evaluations/{evaluationId}/vote", method = RequestMethod.GET)
    public EvaluationVoteDto checkVote(@ApiIgnore JwtUser user,
                                       @PathVariable long evaluationId) {
        Evaluation evaluation = evaluationService.findEvaluation(evaluationId);
        if (evaluation == null) {
            throw new ResourceNotFoundException("Evaluation not found");
        }

        Member member = memberService.getMember(user.getId());

        EvaluationVoteDto dto = new EvaluationVoteDto();
        dto.setEvaluationId(evaluationId);
        dto.setMemberId(user.getId());

        boolean voted = evaluationService.checkVoted(member, evaluation);
        if (voted) {
            dto.setVote(true);
        }

        return dto;
    }

    @RequestMapping(value = "/evaluations/{evaluationId}/comments", method = RequestMethod.POST)
    public EvaluationDto createComment(@ApiIgnore JwtUser user,
                                       @PathVariable long evaluationId,
                                       @RequestBody @Valid CommentDto commentDto) {
        Evaluation evaluation = evaluationService.findEvaluation(evaluationId);
        if (evaluation == null) {
            throw new ResourceNotFoundException("Evaluation not found");
        }

        Member member = memberService.getMember(user.getId());

        Comment comment = commentDto.toEntity();
        comment.setCreatedBy(member);

        commentService.saveComment(evaluation, comment);

        Evaluation updated = evaluationService.getEvaluation(evaluationId);
        return new EvaluationDto(updated);
    }

    @RequestMapping(value = "/evaluations/{evaluationId}/comments", method = RequestMethod.GET)
    public Page<CommentDto> findComments(@PathVariable long evaluationId,
                                         @PageableDefault Pageable pageable) {
        Evaluation evaluation = evaluationService.findEvaluation(evaluationId);
        if (evaluation == null) {
            throw new ResourceNotFoundException("Evaluation not found");
        }

        return commentService.findByEvaluation(evaluation, pageable).map(CommentDto::new);
    }
}
