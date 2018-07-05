package io.scinapse.api.repository;

import io.scinapse.api.model.Comment;
import io.scinapse.api.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
    Page<Comment> findByPaperIdOrderByUpdatedAtDesc(long paperId, Pageable pageable);
    long countByCreatedBy(Member createdBy);
}
