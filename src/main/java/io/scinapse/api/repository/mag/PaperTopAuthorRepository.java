package io.scinapse.api.repository.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.model.mag.PaperTopAuthor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@XRayEnabled
public interface PaperTopAuthorRepository extends JpaRepository<PaperTopAuthor, PaperTopAuthor.PaperTopAuthorId> {
    List<PaperTopAuthor> findByPaperIdIn(List<Long> paperIds);
}
