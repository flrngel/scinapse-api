package network.pluto.absolute.controller;

import network.pluto.absolute.dto.ArticleDto;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.ArticleService;
import network.pluto.absolute.service.EvaluationService;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Article;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
public class ArticleController {

    private final MemberService memberService;
    private final ArticleService articleService;
    private final EvaluationService evaluationService;

    @Autowired
    public ArticleController(MemberService memberService,
                             ArticleService articleService,
                             EvaluationService evaluationService) {
        this.memberService = memberService;
        this.articleService = articleService;
        this.evaluationService = evaluationService;
    }

    @RequestMapping(value = "/articles", method = RequestMethod.POST)
    public ArticleDto createArticle(@ApiIgnore JwtUser user,
                                    @RequestBody @Valid ArticleDto articleDto) {
        Member member = memberService.getMember(user.getId());

        Article article = articleDto.toEntity();
        article.setCreatedBy(member);

        article = articleService.saveArticle(article);

        // increase member reputation
        memberService.increaseReputation(member, 10);

        return new ArticleDto(article);
    }

    @RequestMapping(value = "/articles", method = RequestMethod.GET)
    public Page<ArticleDto> getArticles(@ApiIgnore JwtUser user,
                                        @RequestParam(required = false) List<Long> ids,
                                        @PageableDefault Pageable pageable) {
        Page<Article> articles;

        if (!CollectionUtils.isEmpty(ids)) {
            articles = articleService.findArticlesIn(ids, pageable);
        } else {
            articles = articleService.findArticles(pageable);
        }

        Page<ArticleDto> articleDtos = articles.map(ArticleDto::new);

        if (user != null) {
            Member member = memberService.getMember(user.getId());

            Map<Long, Boolean> evaluatedMap = evaluationService.checkEvaluated(member, articles.getContent());
            articleDtos.forEach(dto -> {
                if (evaluatedMap.get(dto.getId())) {
                    dto.setEvaluated(true);
                }
            });
        }

        return articleDtos;
    }

    @RequestMapping(value = "/articles/{articleId}", method = RequestMethod.GET)
    public ArticleDto getArticle(@ApiIgnore JwtUser user,
                                 @PathVariable long articleId) {
        Article article = articleService.findArticle(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("Article not found");
        }

        ArticleDto articleDto = new ArticleDto(article, true);

        if (user != null) {
            Member member = memberService.getMember(user.getId());

            boolean evaluated = evaluationService.checkEvaluated(member, article);
            if (evaluated) {
                articleDto.setEvaluated(true);
            }

            Map<Long, Boolean> votedMap = evaluationService.checkVoted(member, article.getEvaluations());
            articleDto.getEvaluations().forEach(e -> {
                if (votedMap.get(e.getId())) {
                    e.setVoted(true);
                }
            });
        }

        return articleDto;
    }

    @RequestMapping(value = "/articles/{articleId}", params = "simple", method = RequestMethod.GET)
    public ArticleDto getSimpleArticle(@PathVariable long articleId) {
        Article article = articleService.findArticle(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("Article not found");
        }

        return new ArticleDto(article, false);
    }
}
