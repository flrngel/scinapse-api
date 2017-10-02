package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import network.pluto.bibliotheca.enums.ArticleSource;
import network.pluto.bibliotheca.enums.ArticleType;
import network.pluto.bibliotheca.models.Article;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Data
public class ArticleDto {

    @ApiModelProperty(readOnly = true)
    private Long id;

    @ApiModelProperty(required = true)
    private ArticleType type;

    @ApiModelProperty(required = true)
    private String title;

    private String articleAbstract;
    private String summary;
    private String link;
    private String source;
    private LocalDateTime articlePublishedAt;
    private LocalDateTime articleUpdatedAt;

    @ApiModelProperty(readOnly = true)
    private ArticlePointDto articlePoint;

    @ApiModelProperty(readOnly = true)
    private MemberDto createdBy;

    private List<AuthorDto> authors;

    @ApiModelProperty(readOnly = true)
    private List<EvaluationDto> evaluations;

    public ArticleDto(Article article) {
        this.id = article.getArticleId();
        this.type = article.getType();
        this.title = article.getTitle();
        this.articleAbstract = article.getArticleAbstract();
        this.summary = article.getSummary();
        this.link = article.getSummary();
        this.source = article.getSource().name();
        this.articlePublishedAt = article.getArticlePublishedAt();
        this.articleUpdatedAt = article.getArticleUpdatedAt();

        if (article.getPoint() != null) {
            this.articlePoint = new ArticlePointDto(article.getPoint());
        }

        if (article.getMember() != null) {
            this.createdBy = new MemberDto(article.getMember());
        }

        if (article.getAuthors() != null) {
            this.authors = article.getAuthors().stream().map(AuthorDto::new).collect(Collectors.toList());
        }

        if (article.getEvaluations() != null) {
            this.evaluations = article.getEvaluations().stream().map(EvaluationDto::new).collect(Collectors.toList());
        }
    }

    public Article toEntity() {
        Article article = new Article();
        article.setType(this.type);
        article.setTitle(this.title);
        article.setArticleAbstract(this.articleAbstract);
        article.setSummary(this.summary);
        article.setLink(this.link);
        article.setSource(ArticleSource.valueOf(this.source));
        article.setArticlePublishedAt(this.articlePublishedAt);
        article.setArticleUpdatedAt(this.articleUpdatedAt);

        if (this.articlePoint != null) {
            article.setPoint(this.articlePoint.toEntity());
        }
        article.setMember(this.createdBy.toEntity());

        if (this.authors != null) {
            article.setAuthors(this.authors.stream().map(AuthorDto::toEntity).collect(Collectors.toList()));
        }

        return article;
    }
}
