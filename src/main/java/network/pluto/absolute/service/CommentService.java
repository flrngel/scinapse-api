package network.pluto.absolute.service;

import lombok.RequiredArgsConstructor;
import network.pluto.bibliotheca.models.Comment;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.repositories.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    @Transactional
    public Comment saveComment(long paperId, @Nonnull Comment comment) {
        comment.setPaperId(paperId);
        return commentRepository.save(comment);
    }

    public Comment find(long commentId) {
        return commentRepository.findOne(commentId);
    }

    public Page<Comment> findByPaperId(long paperId, Pageable pageable) {
        return commentRepository.findByPaperIdOrderByIdDesc(paperId, pageable);
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

}
