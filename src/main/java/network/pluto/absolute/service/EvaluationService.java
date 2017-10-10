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

    public Evaluation increaseVote(long evaluationId, Member member) {
        Evaluation one = this.evaluationRepository.getOne(evaluationId);

        EvaluationVote vote = new EvaluationVote();
        vote.setMember(member);
        vote.setEvaluation(one);
        evaluationVoteRepository.save(vote);

        one.setVote(one.getVote() + 1);
        return one;
    }

    public EvaluationVote checkVote(Member member, long evaluationId) {
        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationId(evaluationId);

        return evaluationVoteRepository.findByMemberAndEvaluation(member, evaluation);
    }
}
