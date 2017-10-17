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
            point.setContribution(0.0);
            point.setAnalysis(0.0);
            point.setExpressiveness(0.0);
            point.updateTotal();
            article.setPoint(point);
        }

        long count = evaluationRepository.countByArticle(article);

        ArticlePoint articlePoint = article.getPoint();
        EvaluationPoint evaluationPoint = save.getPoint();

        articlePoint.setOriginality((articlePoint.getOriginality() + evaluationPoint.getOriginality()) / count);
        articlePoint.setContribution((articlePoint.getContribution() + evaluationPoint.getContribution()) / count);
        articlePoint.setAnalysis((articlePoint.getAnalysis() + evaluationPoint.getAnalysis()) / count);
        articlePoint.setExpressiveness((articlePoint.getExpressiveness() + evaluationPoint.getExpressiveness()) / count);

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

    public boolean checkEvaluated(Article article, Member member) {
        return evaluationRepository.existsByArticleAndCreatedBy(article, member);
    }
}
