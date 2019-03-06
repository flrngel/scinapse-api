package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.data.scinapse.model.Authority;
import io.scinapse.domain.enums.AuthorityName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Authority findByName(AuthorityName name);
}
