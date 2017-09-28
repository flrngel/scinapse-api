package network.pluto.absolute.controller;

import network.pluto.absolute.dto.ArticleDto;
import network.pluto.absolute.service.ArticleService;
import network.pluto.bibliotheca.models.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ArticleController {

    private final ArticleService articleService;

    @Autowired
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @RequestMapping(value = "/article/{articleId}", method = RequestMethod.GET)
    public ArticleDto getArticle(@PathVariable long articleId) {
        Article article = this.articleService.getArticle(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("Article not found.");
        }
        return new ArticleDto(article);
    }

    @RequestMapping(value = "/articles", method = RequestMethod.POST)
    public ArticleDto createArticle(@RequestBody ArticleDto articleDto) {
        Article article = this.articleService.saveArticle(articleDto.toEntity());

        return new ArticleDto(article);
    }

    @RequestMapping(value = "/articles", method = RequestMethod.GET)
    public List<ArticleDto> getArticles() {
        return this.articleService.getArticles().stream().map(ArticleDto::new).collect(Collectors.toList());
    }
}
