package network.pluto.absolute.service;

import lombok.NonNull;
import network.pluto.bibliotheca.models.*;
import network.pluto.bibliotheca.repositories.ReviewRepository;
import network.pluto.bibliotheca.repositories.ReviewVoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewVoteRepository reviewVoteRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                         ReviewVoteRepository reviewVoteRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewVoteRepository = reviewVoteRepository;
    }

    @Transactional
    public Review saveReview(Article article, @NonNull Review review) {
        review.setArticle(article);
        Review save = reviewRepository.save(review);

        updateArticlePoint(article, save);
        article.increaseReviewSize();

        return save;
    }

    private void updateArticlePoint(Article article, Review save) {
        if (article.getPoint() == null) {
            ArticlePoint point = new ArticlePoint();
            point.setOriginality(0.0);
            point.setSignificance(0.0);
            point.setValidity(0.0);
            point.setOrganization(0.0);
            point.updateTotal();
            article.setPoint(point);
        }

        long count = reviewRepository.countByArticle(article);

        ArticlePoint articlePoint = article.getPoint();
        ReviewPoint reviewPoint = save.getPoint();

        articlePoint.setOriginality((articlePoint.getOriginality() * (count - 1) + reviewPoint.getOriginality()) / count);
        articlePoint.setSignificance((articlePoint.getSignificance() * (count - 1) + reviewPoint.getSignificance()) / count);
        articlePoint.setValidity((articlePoint.getValidity() * (count - 1) + reviewPoint.getValidity()) / count);
        articlePoint.setOrganization((articlePoint.getOrganization() * (count - 1) + reviewPoint.getOrganization()) / count);

        articlePoint.updateTotal();
    }

    public Review findReview(long reviewId) {
        return reviewRepository.findOne(reviewId);
    }

    public Review getReview(long reviewId) {
        return reviewRepository.getOne(reviewId);
    }

    public Page<Review> findByCreatedBy(Member createdBy, Pageable pageable) {
        return reviewRepository.findByCreatedBy(createdBy, pageable);
    }

    public Page<Review> findByArticle(Article article, Pageable pageable) {
        return reviewRepository.findByArticle(article, pageable);
    }

    public long getCount(Member createdBy) {
        return reviewRepository.countByCreatedBy(createdBy);
    }

    @Transactional
    public void increaseVote(Review review, Member member) {
        ReviewVote vote = new ReviewVote();
        vote.setMember(member);
        vote.setReview(review);
        reviewVoteRepository.save(vote);

        review.increaseVote();
    }

    public boolean checkVoted(Member member, Review review) {
        return reviewVoteRepository.existsByMemberAndReview(member, review);
    }

    // Map<ReviewId, voted>
    public Map<Long, Boolean> checkVoted(Member member, List<Review> reviews) {
        Map<Long, Boolean> votedMap = reviewVoteRepository
                .findByMemberAndReviewIn(member, reviews)
                .stream()
                .map(ev -> ev.getReview().getReviewId())
                .collect(Collectors.toMap(
                        e -> e,
                        e -> true
                ));

        List<Long> reviewIds = reviews.stream().map(Review::getReviewId).collect(Collectors.toList());
        for (Long id : reviewIds) {
            votedMap.putIfAbsent(id, false);
        }

        return votedMap;
    }

    public boolean checkEvaluated(Member member, Article article) {
        return reviewRepository.existsByCreatedByAndArticle(member, article);
    }

    // Map<ArticleId, evaluated>
    public Map<Long, Boolean> checkEvaluated(Member member, List<Article> articles) {
        Map<Long, Boolean> evaluatedMap = reviewRepository
                .findByCreatedByAndArticleIn(member, articles)
                .stream()
                .map(e -> e.getArticle().getArticleId())
                .collect(Collectors.toMap(
                        id -> id,
                        id -> true
                ));

        List<Long> articleIds = articles.stream().map(Article::getArticleId).collect(Collectors.toList());
        for (Long id : articleIds) {
            evaluatedMap.putIfAbsent(id, false);
        }

        return evaluatedMap;
    }
}
