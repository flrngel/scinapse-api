package network.pluto.absolute.service;

import lombok.NonNull;
import network.pluto.bibliotheca.models.Comment;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.Review;
import network.pluto.bibliotheca.repositories.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Transactional
    public Comment saveComment(Review review, @NonNull Comment comment) {
        comment.setReview(review);
        Comment save = commentRepository.save(comment);

        review.increaseCommentSize();

        return save;
    }

    public Page<Comment> findByReview(Review review, Pageable pageable) {
        return commentRepository.findByReview(review, pageable);
    }

    public long getCount(Member createdBy) {
        return commentRepository.countByCreatedBy(createdBy);
    }
}
