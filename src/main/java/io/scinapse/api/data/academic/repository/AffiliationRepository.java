package io.scinapse.api.data.academic.repository;

import io.scinapse.api.data.academic.Affiliation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AffiliationRepository extends JpaRepository<Affiliation, Long> {
}
