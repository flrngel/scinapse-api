package io.scinapse.api.repository.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.model.mag.AuthorTopPaper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@XRayEnabled
public interface AuthorTopPaperRepository extends JpaRepository<AuthorTopPaper, AuthorTopPaper.AuthorTopPaperId> {
    List<AuthorTopPaper> findByAuthorIdAndPaperIdNot(long authorId, long paperId);
}
