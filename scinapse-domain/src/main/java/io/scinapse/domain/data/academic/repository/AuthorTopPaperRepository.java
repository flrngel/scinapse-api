package io.scinapse.domain.data.academic.repository;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.domain.data.academic.AuthorTopPaper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@XRayEnabled
public interface AuthorTopPaperRepository extends JpaRepository<AuthorTopPaper, AuthorTopPaper.AuthorTopPaperId> {
    List<AuthorTopPaper> findByAuthorIdAndPaperIdNot(long authorId, long paperId);
    List<AuthorTopPaper> findByAuthorIdIn(List<Long> authorIds);
}
