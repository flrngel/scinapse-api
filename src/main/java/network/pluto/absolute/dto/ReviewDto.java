package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import network.pluto.bibliotheca.models.Review;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@ToString(exclude = { "comments", "createdBy" })
@Getter
@Setter
public class ReviewDto {

    @ApiModelProperty(readOnly = true)
    private long id;

    @ApiModelProperty(required = true)
    @NotNull
    @Valid
    private ReviewPointDto point;

    @ApiModelProperty(readOnly = true)
    private long articleId;

    @ApiModelProperty(readOnly = true)
    private int vote;

    @ApiModelProperty(readOnly = true)
    private boolean voted;

    @ApiModelProperty(readOnly = true)
    private List<CommentDto> comments;

    @ApiModelProperty(readOnly = true)
    private int commentSize;

    @ApiModelProperty(readOnly = true)
    private MemberDto createdBy;

    @ApiModelProperty(readOnly = true)
    private LocalDateTime createdAt;

    public ReviewDto(Review review, boolean voted) {
        this.id = review.getReviewId();
        this.articleId = review.getArticle().getArticleId();
        this.vote = review.getVote();
        this.commentSize = review.getCommentSize();
        this.createdBy = new MemberDto(review.getCreatedBy());
        this.createdAt = review.getCreatedAt();

        this.voted = voted;

        if (review.getPoint() != null) {
            this.point = new ReviewPointDto(review.getPoint());
        }

        if (review.getComments() != null) {
            this.comments = review.getComments().stream().map(CommentDto::new).collect(Collectors.toList());
        }
    }

    public ReviewDto(Review review) {
        this(review, false);
    }

    public Review toEntity() {
        Review review = new Review();

        if (this.point != null) {
            review.setPoint(this.point.toEntity());
        }

        return review;
    }
}
