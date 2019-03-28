package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.data.scinapse.model.PaperRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaperRequestRepository extends JpaRepository<PaperRequest, Long> {
}
