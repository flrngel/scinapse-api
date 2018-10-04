package io.scinapse.api.repository.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.model.mag.Paper;
import io.scinapse.api.model.mag.PaperAuthor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@XRayEnabled
public interface PaperAuthorRepository extends JpaRepository<PaperAuthor, PaperAuthor.PaperAuthorId> {

    @Query("select r.paper from PaperAuthor r join r.paper where r.id.authorId = :authorId order by r.paper.citationCount desc")
    List<Paper> getAuthorPapersMostCitations(@Param("authorId") long authorId, Pageable pageable);

    @Query("select r.paper from PaperAuthor r join r.paper where r.id.authorId = :authorId order by r.paper.year desc")
    List<Paper> getAuthorPapersNewest(@Param("authorId") long authorId, Pageable pageable);

    @Query("select r.paper from PaperAuthor r join r.paper where r.id.authorId = :authorId order by r.paper.year asc")
    List<Paper> getAuthorPapersOldest(@Param("authorId") long authorId, Pageable pageable);

    Page<PaperAuthor> getByPaperIdOrderByAuthorSequenceNumber(long paperId, Pageable pageable);

}
