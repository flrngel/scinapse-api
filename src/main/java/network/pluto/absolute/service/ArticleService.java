package network.pluto.absolute.service;

import network.pluto.bibliotheca.models.Article;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.repositories.ArticleRepository;
import network.pluto.bibliotheca.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public ArticleService(ArticleRepository articleRepository, MemberRepository memberRepository) {
        this.articleRepository = articleRepository;
        this.memberRepository = memberRepository;
    }

    public Article getArticle(long id) {
        return this.articleRepository.findOne(id);
    }

    @Transactional
    public Article saveArticle(Article article) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String memberName = authentication.getName();
        Member member = this.memberRepository.findByEmail(memberName);
        article.setMember(member);
        return this.articleRepository.save(article);
    }

    public List<Article> getArticles() {
        return this.articleRepository.findAll();
    }
}
