package network.pluto.absolute.service;

import lombok.NonNull;
import network.pluto.bibliotheca.models.Article;
import network.pluto.bibliotheca.repositories.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Article getArticle(long id) {
        return this.articleRepository.findOne(id);
    }

    @Transactional
    public Article saveArticle(@NonNull Article article) {
        return this.articleRepository.save(article);
    }

    public List<Article> getArticles() {
        return this.articleRepository.findAll();
    }
}
