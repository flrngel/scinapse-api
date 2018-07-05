package io.scinapse.api.repository;

import io.scinapse.api.enums.AuthorityName;
import io.scinapse.api.model.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Authority findByName(AuthorityName name);
}
