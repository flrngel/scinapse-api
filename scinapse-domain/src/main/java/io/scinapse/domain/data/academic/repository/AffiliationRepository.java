package io.scinapse.domain.data.academic.repository;

import io.scinapse.domain.data.academic.Affiliation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AffiliationRepository extends JpaRepository<Affiliation, Long> {
}
