package network.pluto.absolute.service;

import lombok.NonNull;
import network.pluto.bibliotheca.models.Article;
import network.pluto.bibliotheca.models.Evaluation;
import network.pluto.bibliotheca.models.EvaluationVote;
import network.pluto.bibliotheca.models.Member;
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

        return this.evaluationRepository.save(evaluation);
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
