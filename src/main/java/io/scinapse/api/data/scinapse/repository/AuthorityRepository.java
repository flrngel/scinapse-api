package io.scinapse.api.data.scinapse.repository;

import io.scinapse.api.data.scinapse.model.Authority;
import io.scinapse.api.enums.AuthorityName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Authority findByName(AuthorityName name);
}
