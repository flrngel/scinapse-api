package io.scinapse.api.repository.mag;

import io.scinapse.api.model.mag.Affiliation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AffiliationRepository extends JpaRepository<Affiliation, Long> {
}
