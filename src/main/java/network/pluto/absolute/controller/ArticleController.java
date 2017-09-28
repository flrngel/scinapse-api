package network.pluto.absolute.controller;

import network.pluto.absolute.dto.ArticleDto;
import network.pluto.absolute.security.jwt.JwtAuthenticationToken;
import network.pluto.absolute.service.ArticleService;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Article;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ArticleController {

    private final MemberService memberService;
    private final ArticleService articleService;

    @Autowired
    public ArticleController(MemberService memberService, ArticleService articleService) {
        this.memberService = memberService;
        this.articleService = articleService;
    }

    @RequestMapping(value = "/articles", method = RequestMethod.POST)
    public ArticleDto createArticle(Principal principal,
                                    @RequestBody ArticleDto articleDto) {
        Article article = articleDto.toEntity();

        Member member = (Member) ((JwtAuthenticationToken) principal).getPrincipal();
        member = this.memberService.findByEmail(member.getEmail());
        article.setMember(member);

        article = this.articleService.saveArticle(article);

        return new ArticleDto(article);
    }

    @RequestMapping(value = "/articles", method = RequestMethod.GET)
    public List<ArticleDto> getArticles() {
        return this.articleService.getArticles().stream().map(ArticleDto::new).collect(Collectors.toList());
    }

    @RequestMapping(value = "/article/{articleId}", method = RequestMethod.GET)
    public ArticleDto getArticle(@PathVariable long articleId) {
        Article article = this.articleService.getArticle(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("Article not found.");
        }
        return new ArticleDto(article);
    }
}
