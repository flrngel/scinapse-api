package network.pluto.absolute.repositories;

import network.pluto.absolute.models.Comment;
import network.pluto.absolute.models.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
    Page<Comment> findByPaperIdOrderByUpdatedAtDesc(long paperId, Pageable pageable);
    long countByCreatedBy(Member createdBy);
}
