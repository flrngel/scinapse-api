package io.scinapse.api.repository.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.model.mag.PaperReference;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@XRayEnabled
public interface PaperReferenceRepository extends JpaRepository<PaperReference, PaperReference.PaperReferenceId> {
    List<PaperReference> findByPaperId(long paperId, Pageable pageable);
    List<PaperReference> findByPaperReferenceId(long referenceId, Pageable pageable);
}
