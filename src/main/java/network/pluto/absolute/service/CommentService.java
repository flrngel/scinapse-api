package network.pluto.absolute.service;

import lombok.NonNull;
import network.pluto.bibliotheca.models.Comment;
import network.pluto.bibliotheca.models.Evaluation;
import network.pluto.bibliotheca.repositories.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Comment saveComment(Evaluation evaluation, @NonNull Comment comment) {
        comment.setEvaluation(evaluation);
        return this.commentRepository.save(comment);
    }
}
