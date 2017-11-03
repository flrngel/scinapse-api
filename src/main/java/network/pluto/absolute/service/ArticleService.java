package network.pluto.absolute.service;

import com.querydsl.core.types.Predicate;
import lombok.NonNull;
import network.pluto.bibliotheca.models.Article;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.repositories.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    @Autowired
    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public Article findArticle(long id) {
        return articleRepository.findOne(id);
    }

    public Article getArticle(long id) {
        return articleRepository.getOne(id);
    }

    @Transactional
    public Article saveArticle(@NonNull Article article) {
        return articleRepository.save(article);
    }

    public Page<Article> findArticles(Predicate predicate, Pageable pageable) {
        return articleRepository.findAll(predicate, pageable);
    }

    public Page<Article> findArticlesIn(List<Long> ids, Pageable pageable) {
        return articleRepository.findByArticleIdIn(ids, pageable);
    }

    public Page<Article> findByCreatedBy(Member createdBy, Pageable pageable) {
        return articleRepository.findByCreatedBy(createdBy, pageable);
    }

    public long getCount(Member createdBy) {
        return articleRepository.countByCreatedBy(createdBy);
    }
}
