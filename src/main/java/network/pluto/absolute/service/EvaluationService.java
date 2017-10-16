package network.pluto.absolute.service;

import lombok.NonNull;
import network.pluto.bibliotheca.models.*;
import network.pluto.bibliotheca.repositories.ArticleRepository;
import network.pluto.bibliotheca.repositories.EvaluationRepository;
import network.pluto.bibliotheca.repositories.EvaluationVoteRepository;
import network.pluto.bibliotheca.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EvaluationService {

    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final EvaluationRepository evaluationRepository;
    private final EvaluationVoteRepository evaluationVoteRepository;

    @Autowired
    public EvaluationService(MemberRepository memberRepository,
                             ArticleRepository articleRepository,
                             EvaluationRepository evaluationRepository,
                             EvaluationVoteRepository evaluationVoteRepository) {
        this.memberRepository = memberRepository;
        this.articleRepository = articleRepository;
        this.evaluationRepository = evaluationRepository;
        this.evaluationVoteRepository = evaluationVoteRepository;
    }

    @Transactional
    public Evaluation saveEvaluation(long articleId, @NonNull Evaluation evaluation) {
        Article article = this.articleRepository.getOne(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("Article not found");
        }
        evaluation.setArticle(article);
        Evaluation save = this.evaluationRepository.save(evaluation);

        updateArticlePoint(article, save);

        return save;
    }

    private void updateArticlePoint(Article article, Evaluation save) {
        if (article.getPoint() == null) {
            ArticlePoint point = new ArticlePoint();
            point.setOriginality(0.0);
            point.setContribution(0.0);
            point.setAnalysis(0.0);
            point.setExpressiveness(0.0);
            point.updateTotal();
            article.setPoint(point);
        }

        long count = this.evaluationRepository.countByArticle(article);

        ArticlePoint articlePoint = article.getPoint();
        EvaluationPoint evaluationPoint = save.getPoint();

        articlePoint.setOriginality((articlePoint.getOriginality() + evaluationPoint.getOriginality()) / count);
        articlePoint.setContribution((articlePoint.getContribution() + evaluationPoint.getContribution()) / count);
        articlePoint.setAnalysis((articlePoint.getAnalysis() + evaluationPoint.getAnalysis()) / count);
        articlePoint.setExpressiveness((articlePoint.getExpressiveness() + evaluationPoint.getExpressiveness()) / count);

        articlePoint.updateTotal();
    }

    public List<Evaluation> getEvaluations(long articleId) {
        Article article = this.articleRepository.getOne(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("Article not found");
        }
        return article.getEvaluations();
    }

    public Evaluation getEvaluation(long evaluationId) {
        return this.evaluationRepository.getOne(evaluationId);
    }

    public List<Evaluation> findByCreatedBy(Member createdBy) {
        return evaluationRepository.findByCreatedBy(createdBy);
    }

    public Evaluation increaseVote(long evaluationId, Member member) {
        Evaluation one = this.evaluationRepository.getOne(evaluationId);

        EvaluationVote vote = new EvaluationVote();
        vote.setMember(member);
        vote.setEvaluation(one);
        evaluationVoteRepository.save(vote);

        one.setVote(one.getVote() + 1);
        return one;
    }

    public boolean checkVoted(long memberId, long evaluationId) {
        Member member = new Member();
        member.setMemberId(memberId);

        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationId(evaluationId);

        return evaluationVoteRepository.existsByMemberAndEvaluation(member, evaluation);
    }

    // Map<EvaluationId, voted>
    public Map<Long, Boolean> checkVoted(long memberId, List<Long> evaluationIds) {
        Map<Long, Boolean> votedMap = evaluationVoteRepository
                .getVotedEvaluationIds(memberId, evaluationIds)
                .stream()
                .collect(Collectors.toMap(
                        e -> e,
                        e -> true
                ));

        for (Long id : evaluationIds) {
            votedMap.putIfAbsent(id, false);
        }

        return votedMap;
    }

    public boolean checkEvaluated(long articleId, long memberId) {
        Article article = new Article();
        article.setArticleId(articleId);

        Member member = new Member();
        member.setMemberId(memberId);

        return evaluationRepository.existsByArticleAndCreatedBy(article, member);
    }
}
