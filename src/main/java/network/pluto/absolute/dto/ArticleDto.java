package network.pluto.absolute.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import network.pluto.bibliotheca.enums.ArticleSource;
import network.pluto.bibliotheca.enums.ArticleType;
import network.pluto.bibliotheca.models.Article;
import org.hibernate.validator.constraints.URL;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@ToString(exclude = { "createdBy", "authors", "reviews" })
@Getter
@Setter
public class ArticleDto {

    @ApiModelProperty(readOnly = true)
    private Long id;

    @ApiModelProperty(required = true)
    @NotNull
    private ArticleType type;

    @ApiModelProperty(required = true)
    @NotNull
    private String title;

    private String summary;

    @URL
    private String link;

    private ArticleSource source;

    private String note;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime articlePublishedAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime articleUpdatedAt;

    @ApiModelProperty(readOnly = true)
    private ArticlePointDto point;

    @ApiModelProperty(readOnly = true)
    private MemberDto createdBy;

    @ApiModelProperty(readOnly = true)
    private LocalDateTime createdAt;

    @Valid
    private List<AuthorDto> authors;

    @ApiModelProperty(readOnly = true)
    private List<ReviewDto> reviews;

    @ApiModelProperty(readOnly = true)
    private int reviewSize;

    @ApiModelProperty(readOnly = true)
    private boolean evaluated;

    public ArticleDto(Article article, boolean loadReviews) {
        this.id = article.getId();
        this.type = article.getType();
        this.title = article.getTitle();
        this.summary = article.getSummary();
        this.link = article.getLink();
        this.source = article.getSource();
        this.note = article.getNote();
        this.reviewSize = article.getReviewSize();
        this.createdBy = new MemberDto(article.getCreatedBy());
        this.createdAt = article.getCreatedAt();

        if (article.getPoint() != null) {
            this.point = new ArticlePointDto(article.getPoint());
        }

        if (article.getAuthors() != null) {
            this.authors = article.getAuthors().stream().map(AuthorDto::new).collect(Collectors.toList());
        }

        if (loadReviews && article.getReviews() != null) {
            this.reviews = article.getReviews().stream().map(ReviewDto::new).collect(Collectors.toList());
        }
    }

    public ArticleDto(Article article) {
        this(article, false);
    }

    public Article toEntity() {
        Article article = new Article();
        article.setType(this.type);
        article.setTitle(this.title);
        article.setSummary(this.summary);
        article.setLink(this.link);
        article.setSource(this.source);
        article.setNote(this.note);
        article.setArticlePublishedAt(this.articlePublishedAt);
        article.setArticleUpdatedAt(this.articleUpdatedAt);

        if (this.authors != null) {
            article.setAuthors(this.authors.stream().map(AuthorDto::toEntity).collect(Collectors.toList()));
        }

        return article;
    }
}
