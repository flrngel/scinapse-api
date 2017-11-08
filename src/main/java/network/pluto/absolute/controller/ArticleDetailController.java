package network.pluto.absolute.controller;

import network.pluto.absolute.dto.CommentDto;
import network.pluto.absolute.dto.ReviewDto;
import network.pluto.absolute.dto.ReviewVoteDto;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.ArticleService;
import network.pluto.absolute.service.CommentService;
import network.pluto.absolute.service.MemberService;
import network.pluto.absolute.service.ReviewService;
import network.pluto.bibliotheca.enums.ReputationChangeReason;
import network.pluto.bibliotheca.models.Article;
import network.pluto.bibliotheca.models.Comment;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/articles/{articleId}")
public class ArticleDetailController {

    private final MemberService memberService;
    private final ArticleService articleService;
    private final ReviewService reviewService;
    private final CommentService commentService;

    @Autowired
    public ArticleDetailController(MemberService memberService,
                                   ArticleService articleService,
                                   ReviewService reviewService,
                                   CommentService commentService) {
        this.memberService = memberService;
        this.articleService = articleService;
        this.reviewService = reviewService;
        this.commentService = commentService;
    }

    @RequestMapping(value = "/reviews", method = RequestMethod.POST)
    public ReviewDto createReview(@ApiIgnore JwtUser user,
                                  @PathVariable long articleId,
                                  @RequestBody @Valid ReviewDto reviewDto) {
        Article article = articleService.findArticle(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("Article not found");
        }

        Member member = memberService.getMember(user.getId());

        boolean evaluated = reviewService.checkEvaluated(member, article);
        if (evaluated) {
            throw new BadRequestException("Already evaluated");
        }

        Review review = reviewDto.toEntity();
        review.setCreatedBy(member);

        review = reviewService.saveReview(article, review);

        // increase member reputation
        memberService.changeReputation(member, ReputationChangeReason.REVIEW_CREATE, 5);

        return new ReviewDto(review);
    }

    @RequestMapping(value = "/reviews", method = RequestMethod.GET)
    public Page<ReviewDto> findReviews(@ApiIgnore JwtUser user,
                                       @PathVariable long articleId,
                                       @PageableDefault Pageable pageable) {
        Article article = articleService.findArticle(articleId);
        if (article == null) {
            throw new ResourceNotFoundException("Article not found");
        }

        Page<Review> reviews = reviewService.findByArticle(article, pageable);
        Page<ReviewDto> dtoList = reviews.map(ReviewDto::new);

        if (user != null) {
            Member member = memberService.getMember(user.getId());

            Map<Long, Boolean> votedMap = reviewService.checkVoted(member, reviews.getContent());
            dtoList.forEach(e -> {
                if (votedMap.get(e.getId())) {
                    e.setVoted(true);
                }
            });
        }

        return dtoList;
    }

    @RequestMapping(value = "/reviews/{reviewId}/vote", method = RequestMethod.POST)
    public ReviewVoteDto pressVote(@ApiIgnore JwtUser user,
                                   @PathVariable long reviewId) {
        Review review = reviewService.findReview(reviewId);
        if (review == null) {
            throw new ResourceNotFoundException("Review not found");
        }

        Member member = memberService.getMember(user.getId());

        boolean voted = reviewService.checkVoted(member, review);
        if (voted) {
            throw new BadRequestException("Already voted");
        }

        // increase review vote number
        reviewService.increaseVote(review, member);

        // increase review creator's reputation
        memberService.changeReputation(review.getCreatedBy(), ReputationChangeReason.REVIEW_VOTED, 1);

        ReviewVoteDto dto = new ReviewVoteDto();
        dto.setReviewId(reviewId);
        dto.setMemberId(user.getId());
        dto.setVote(review.getVote());
        dto.setVoted(true);

        return dto;
    }

    @RequestMapping(value = "/reviews/{reviewId}/vote", method = RequestMethod.GET)
    public ReviewVoteDto checkVote(@ApiIgnore JwtUser user,
                                   @PathVariable long reviewId) {
        Review review = reviewService.findReview(reviewId);
        if (review == null) {
            throw new ResourceNotFoundException("Review not found");
        }

        Member member = memberService.getMember(user.getId());

        ReviewVoteDto dto = new ReviewVoteDto();
        dto.setReviewId(reviewId);
        dto.setMemberId(user.getId());
        dto.setVote(review.getVote());

        boolean voted = reviewService.checkVoted(member, review);
        if (voted) {
            dto.setVoted(true);
        }

        return dto;
    }

    @RequestMapping(value = "/reviews/{reviewId}/comments", method = RequestMethod.POST)
    public CommentDto createComment(@ApiIgnore JwtUser user,
                                    @PathVariable long reviewId,
                                    @RequestBody @Valid CommentDto commentDto) {
        Review review = reviewService.findReview(reviewId);
        if (review == null) {
            throw new ResourceNotFoundException("Review not found");
        }

        Member member = memberService.getMember(user.getId());

        Comment comment = commentDto.toEntity();
        comment.setCreatedBy(member);

        comment = commentService.saveComment(review, comment);
        return new CommentDto(comment);
    }

    @RequestMapping(value = "/reviews/{reviewId}/comments", method = RequestMethod.GET)
    public Page<CommentDto> findComments(@PathVariable long reviewId,
                                         @PageableDefault Pageable pageable) {
        Review review = reviewService.findReview(reviewId);
        if (review == null) {
            throw new ResourceNotFoundException("Review not found");
        }

        return commentService.findByReview(review, pageable).map(CommentDto::new);
    }
}
