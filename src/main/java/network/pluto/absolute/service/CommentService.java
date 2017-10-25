package network.pluto.absolute.service;

import lombok.NonNull;
import network.pluto.bibliotheca.models.Comment;
import network.pluto.bibliotheca.models.Evaluation;
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
    public Comment saveComment(Evaluation evaluation, @NonNull Comment comment) {
        comment.setEvaluation(evaluation);
        Comment save = commentRepository.save(comment);

        evaluation.increaseCommentSize();

        return save;
    }

    public Page<Comment> findByEvaluation(Evaluation evaluation, Pageable pageable) {
        return commentRepository.findByEvaluation(evaluation, pageable);
    }
}
