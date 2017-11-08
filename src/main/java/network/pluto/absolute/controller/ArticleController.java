package network.pluto.absolute.controller;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.Setter;
import network.pluto.absolute.dto.ArticleDto;
import network.pluto.absolute.dto.ArticlePointDto;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.ArticleService;
import network.pluto.absolute.service.MemberService;
import network.pluto.absolute.service.ReviewService;
import network.pluto.bibliotheca.enums.ArticleType;
import network.pluto.bibliotheca.models.Article;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.QArticle;
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
    private final ReviewService reviewService;

    @Autowired
    public ArticleController(MemberService memberService,
                             ArticleService articleService,
                             ReviewService reviewService) {
        this.memberService = memberService;
        this.articleService = articleService;
        this.reviewService = reviewService;
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
                                        ArticleQuery query,
                                        @PageableDefault Pageable pageable) {
        Page<Article> articles = articleService.findArticles(query.toPredicate(), pageable);

        Page<ArticleDto> articleDtos = articles.map(ArticleDto::new);

        if (user != null) {
            Member member = memberService.getMember(user.getId());

            Map<Long, Boolean> evaluatedMap = reviewService.checkEvaluated(member, articles.getContent());
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

            boolean evaluated = reviewService.checkEvaluated(member, article);
            if (evaluated) {
                articleDto.setEvaluated(true);
            }

            Map<Long, Boolean> votedMap = reviewService.checkVoted(member, article.getReviews());
            articleDto.getReviews().forEach(e -> {
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

    @RequestMapping(value = "/articles/{articleId}/point", method = RequestMethod.GET)
    public ArticlePointDto getPoint(@PathVariable long articleId) {
        Article article = articleService.findArticle(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("Article not found");
        }

        return new ArticlePointDto(article.getPoint());
    }

    @Getter
    @Setter
    public static class ArticleQuery {
        private List<Long> ids;
        private ArticleType type;

        public Predicate toPredicate() {
            QArticle article = QArticle.article;

            BooleanBuilder builder = new BooleanBuilder();

            if (!CollectionUtils.isEmpty(ids)) {
                builder.and(article.id.in(ids));
            }

            if (type != null) {
                builder.and(article.type.eq(type));
            }

            return builder;
        }
    }
}
