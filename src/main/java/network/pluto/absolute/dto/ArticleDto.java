package network.pluto.absolute.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Data
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
    private Double point;

    @ApiModelProperty(readOnly = true)
    private MemberDto createdBy;

    @ApiModelProperty(readOnly = true)
    private LocalDateTime createdAt;

    @Valid
    private List<AuthorDto> authors;

    @ApiModelProperty(readOnly = true)
    private List<EvaluationDto> evaluations;

    @ApiModelProperty(readOnly = true)
    private boolean evaluated;

    public ArticleDto(Article article, boolean loadEvaluations) {
        this.id = article.getArticleId();
        this.type = article.getType();
        this.title = article.getTitle();
        this.summary = article.getSummary();
        this.link = article.getLink();
        this.source = article.getSource();
        this.note = article.getNote();
        this.point = article.getPoint();
        this.createdBy = new MemberDto(article.getCreatedBy());
        this.createdAt = article.getCreatedAt();

        if (article.getAuthors() != null) {
            this.authors = article.getAuthors().stream().map(AuthorDto::new).collect(Collectors.toList());
        }

        if (loadEvaluations && article.getEvaluations() != null) {
            this.evaluations = article.getEvaluations().stream().map(EvaluationDto::new).collect(Collectors.toList());
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
