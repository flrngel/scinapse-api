package network.pluto.absolute.service;

import lombok.NonNull;
import network.pluto.bibliotheca.models.Comment;
import network.pluto.bibliotheca.models.Evaluation;
import network.pluto.bibliotheca.repositories.CommentRepository;
import network.pluto.bibliotheca.repositories.EvaluationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    private final EvaluationRepository evaluationRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(EvaluationRepository evaluationRepository,
                          CommentRepository commentRepository) {
        this.evaluationRepository = evaluationRepository;
        this.commentRepository = commentRepository;
    }


    public Comment saveComment(long evaluationId, @NonNull Comment comment) {
        Evaluation evaluation = this.evaluationRepository.getOne(evaluationId);
        if(evaluation == null) {
            throw new ResourceNotFoundException("Evaluation not found");
        }
        comment.setEvaluation(evaluation);
        return this.commentRepository.save(comment);
    }

    public List<Comment> getComments(long evaluationId) {
        Evaluation evaluation = this.evaluationRepository.getOne(evaluationId);
        if(evaluation == null) {
            throw new ResourceNotFoundException("Evaluation not found");
        }

        return evaluation.getComments();
    }
}
