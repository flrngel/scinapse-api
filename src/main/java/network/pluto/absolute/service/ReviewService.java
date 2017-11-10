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

        addReviewPoint(article, save);
        article.increaseReviewSize();

        return save;
    }

    private void addReviewPoint(Article article, Review save) {
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

    @Transactional
    public void deleteReview(Article article, Review review) {
        deleteReviewPoint(article, review);
        article.decreaseReviewSize();

        deleteAllVotes(review);
        reviewRepository.delete(review);
    }

    private void deleteReviewPoint(Article article, Review review) {
        if (article.getReviewSize() == 1) {
            article.setPoint(null);
            return;
        }

        int count = article.getReviewSize();

        ArticlePoint articlePoint = article.getPoint();
        ReviewPoint reviewPoint = review.getPoint();

        articlePoint.setOriginality((articlePoint.getOriginality() * count - reviewPoint.getOriginality()) / (count - 1));
        articlePoint.setSignificance((articlePoint.getSignificance() * count - reviewPoint.getSignificance()) / (count - 1));
        articlePoint.setValidity((articlePoint.getValidity() * count - reviewPoint.getValidity()) / (count - 1));
        articlePoint.setOrganization((articlePoint.getOrganization() * count - reviewPoint.getOrganization()) / (count - 1));

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

    @Transactional
    public void decreaseVote(Review review, Member member) {
        reviewVoteRepository.deleteByMemberAndReview(member, review);
        review.decreaseVote();
    }

    public void deleteAllVotes(Review review) {
        reviewVoteRepository.deleteByReview(review);
        review.setVote(0);
    }

    public boolean checkVoted(Member member, Review review) {
        return reviewVoteRepository.existsByMemberAndReview(member, review);
    }

    // Map<ReviewId, voted>
    public Map<Long, Boolean> checkVoted(Member member, List<Review> reviews) {
        Map<Long, Boolean> votedMap = reviewVoteRepository
                .findByMemberAndReviewIn(member, reviews)
                .stream()
                .map(ev -> ev.getReview().getId())
                .collect(Collectors.toMap(
                        e -> e,
                        e -> true
                ));

        List<Long> reviewIds = reviews.stream().map(Review::getId).collect(Collectors.toList());
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
                .map(e -> e.getArticle().getId())
                .collect(Collectors.toMap(
                        id -> id,
                        id -> true
                ));

        List<Long> articleIds = articles.stream().map(Article::getId).collect(Collectors.toList());
        for (Long id : articleIds) {
            evaluatedMap.putIfAbsent(id, false);
        }

        return evaluatedMap;
    }
}
