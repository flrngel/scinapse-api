package network.pluto.absolute.service;

import lombok.NonNull;
import network.pluto.bibliotheca.models.*;
import network.pluto.bibliotheca.repositories.EvaluationRepository;
import network.pluto.bibliotheca.repositories.EvaluationVoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final EvaluationVoteRepository evaluationVoteRepository;

    @Autowired
    public EvaluationService(EvaluationRepository evaluationRepository,
                             EvaluationVoteRepository evaluationVoteRepository) {
        this.evaluationRepository = evaluationRepository;
        this.evaluationVoteRepository = evaluationVoteRepository;
    }

    @Transactional
    public Evaluation saveEvaluation(Article article, @NonNull Evaluation evaluation) {
        evaluation.setArticle(article);
        Evaluation save = evaluationRepository.save(evaluation);

        updateArticlePoint(article, save);

        return save;
    }

    private void updateArticlePoint(Article article, Evaluation save) {
        if (article.getPoint() == null) {
            ArticlePoint point = new ArticlePoint();
            point.setOriginality(0.0);
            point.setSignificance(0.0);
            point.setValidity(0.0);
            point.setOrganization(0.0);
            point.updateTotal();
            article.setPoint(point);
        }

        long count = evaluationRepository.countByArticle(article);

        ArticlePoint articlePoint = article.getPoint();
        EvaluationPoint evaluationPoint = save.getPoint();

        articlePoint.setOriginality((articlePoint.getOriginality() + evaluationPoint.getOriginality()) / count);
        articlePoint.setSignificance((articlePoint.getSignificance() + evaluationPoint.getSignificance()) / count);
        articlePoint.setValidity((articlePoint.getValidity() + evaluationPoint.getValidity()) / count);
        articlePoint.setOrganization((articlePoint.getOrganization() + evaluationPoint.getOrganization()) / count);

        articlePoint.updateTotal();
    }

    public Evaluation findEvaluation(long evaluationId) {
        return evaluationRepository.findOne(evaluationId);
    }

    public Evaluation getEvaluation(long evaluationId) {
        return evaluationRepository.getOne(evaluationId);
    }

    public Page<Evaluation> findByCreatedBy(Member createdBy, Pageable pageable) {
        return evaluationRepository.findByCreatedBy(createdBy, pageable);
    }

    public Page<Evaluation> findByArticle(Article article, Pageable pageable) {
        return evaluationRepository.findByArticle(article, pageable);
    }

    public Evaluation increaseVote(Evaluation evaluation, Member member) {
        EvaluationVote vote = new EvaluationVote();
        vote.setMember(member);
        vote.setEvaluation(evaluation);
        evaluationVoteRepository.save(vote);

        evaluation.setVote(evaluation.getVote() + 1);
        return evaluation;
    }

    public boolean checkVoted(Member member, Evaluation evaluation) {
        return evaluationVoteRepository.existsByMemberAndEvaluation(member, evaluation);
    }

    // Map<EvaluationId, voted>
    public Map<Long, Boolean> checkVoted(Member member, List<Evaluation> evaluations) {
        Map<Long, Boolean> votedMap = evaluationVoteRepository
                .findByMemberAndEvaluationIn(member, evaluations)
                .stream()
                .map(ev -> ev.getEvaluation().getEvaluationId())
                .collect(Collectors.toMap(
                        e -> e,
                        e -> true
                ));

        List<Long> evaluationIds = evaluations.stream().map(Evaluation::getEvaluationId).collect(Collectors.toList());
        for (Long id : evaluationIds) {
            votedMap.putIfAbsent(id, false);
        }

        return votedMap;
    }

    public boolean checkEvaluated(Member member, Article article) {
        return evaluationRepository.existsByCreatedByAndArticle(member, article);
    }

    // Map<ArticleId, evaluated>
    public Map<Long, Boolean> checkEvaluated(Member member, List<Article> articles) {
        Map<Long, Boolean> evaluatedMap = evaluationRepository
                .findByCreatedByAndArticleIn(member, articles)
                .stream()
                .map(e -> e.getArticle().getArticleId())
                .collect(Collectors.toMap(
                        id -> id,
                        id -> true
                ));

        List<Long> articleIds = articles.stream().map(Article::getArticleId).collect(Collectors.toList());
        for (Long id : articleIds) {
            evaluatedMap.putIfAbsent(id, false);
        }

        return evaluatedMap;
    }
}
