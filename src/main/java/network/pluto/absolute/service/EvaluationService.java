package network.pluto.absolute.service;

import lombok.NonNull;
import network.pluto.bibliotheca.models.Article;
import network.pluto.bibliotheca.models.Evaluation;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.repositories.ArticleRepository;
import network.pluto.bibliotheca.repositories.EvaluationRepository;
import network.pluto.bibliotheca.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class EvaluationService {
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final EvaluationRepository evaluationRepository;

    @Autowired
    public EvaluationService(MemberRepository memberRepository,
                             ArticleRepository articleRepository,
                             EvaluationRepository evaluationRepository) {
        this.memberRepository = memberRepository;
        this.articleRepository = articleRepository;
        this.evaluationRepository = evaluationRepository;
    }

    @Transactional
    public Evaluation saveEvaluation(long articleId, @NonNull Evaluation evaluation) {
        Article article = this.articleRepository.getOne(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("Article not found");
        }
        evaluation.setArticle(article);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String memberEmail = ((Member) authentication.getPrincipal()).getEmail();
        Member member = this.memberRepository.findByEmail(memberEmail);
        evaluation.setMember(member);

        return this.evaluationRepository.save(evaluation);
    }

    public List<Evaluation> getEvaluations(long articleId) {
        Article article = this.articleRepository.getOne(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("Article not found");
        }
        return this.evaluationRepository.findByArticle(article);
    }
}
