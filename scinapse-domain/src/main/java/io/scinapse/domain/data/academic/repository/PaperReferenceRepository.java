package io.scinapse.domain.data.academic.repository;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.domain.data.academic.model.PaperReference;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@XRayEnabled
public interface PaperReferenceRepository extends JpaRepository<PaperReference, PaperReference.PaperReferenceId> {
    List<PaperReference> findByPaperId(long paperId, Pageable pageable);
    List<PaperReference> findByPaperReferenceId(long referenceId, Pageable pageable);
}
