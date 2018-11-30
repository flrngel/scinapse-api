package io.scinapse.api.data.academic.repository;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.data.academic.PaperTopAuthor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@XRayEnabled
public interface PaperTopAuthorRepository extends JpaRepository<PaperTopAuthor, PaperTopAuthor.PaperTopAuthorId> {
    List<PaperTopAuthor> findByPaperIdIn(List<Long> paperIds);
}
