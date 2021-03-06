package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.data.scinapse.model.Comment;
import io.scinapse.domain.data.scinapse.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
    Page<Comment> findByPaperIdOrderByUpdatedAtDesc(long paperId, Pageable pageable);
    void deleteByCreatedBy(Member createdBy);
}
