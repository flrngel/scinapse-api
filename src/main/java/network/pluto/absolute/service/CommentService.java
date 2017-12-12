package network.pluto.absolute.service;

import lombok.NonNull;
import network.pluto.bibliotheca.models.Comment;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.Paper;
import network.pluto.bibliotheca.models.Review;
import network.pluto.bibliotheca.repositories.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
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

    @Transactional
    public Comment saveComment(Paper paper, @NonNull Comment comment) {
        comment.setPaper(paper);
        return commentRepository.save(comment);
    }

    public Comment find(long commentId) {
        return commentRepository.findOne(commentId);
    }

    public Page<Comment> findByReview(Review review, Pageable pageable) {
        return commentRepository.findByReview(review, pageable);
    }

    public Page<Comment> findByPaper(Paper paper, Pageable pageable) {
        return commentRepository.findByPaper(paper, pageable);
    }

    @Transactional
    public Comment updateComment(Comment old, Comment update) {
        old.setComment(update.getComment());
        return old;
    }

    @Transactional
    public void deleteComment(Comment comment) {
        commentRepository.delete(comment);
    }

    public long getCount(Member createdBy) {
        return commentRepository.countByCreatedBy(createdBy);
    }

    public long getCount(Paper paper) {
        return commentRepository.countByPaper(paper);
    }
}
