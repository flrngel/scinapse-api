package network.pluto.absolute.controller;

import network.pluto.absolute.dto.ArticleDto;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.ArticleService;
import network.pluto.absolute.service.EvaluationService;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Article;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        Member member = this.memberService.getMember(user.getId());

        Article article = articleDto.toEntity();
        article.setCreatedBy(member);

        article = this.articleService.saveArticle(article);

        // increase member reputation
        this.memberService.increaseReputation(member, 10);

        return new ArticleDto(article);
    }

    @RequestMapping(value = "/articles", method = RequestMethod.GET)
    public List<ArticleDto> getArticles() {
        return this.articleService.getArticles().stream().map(ArticleDto::new).collect(Collectors.toList());
    }

    @RequestMapping(value = "/articles/{articleId}", method = RequestMethod.GET)
    public ArticleDto getArticle(@ApiIgnore JwtUser user,
                                 @PathVariable long articleId) {
        Article article = this.articleService.findArticle(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("Article not found");
        }

        ArticleDto articleDto = new ArticleDto(article, true);

        if (user != null) {
            Member member = this.memberService.getMember(user.getId());

            boolean evaluated = evaluationService.checkEvaluated(article, member);
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
}
