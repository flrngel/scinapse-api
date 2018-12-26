package io.scinapse.api.data.scinapse.repository.author;

import io.scinapse.api.data.scinapse.model.author.AuthorAward;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorAwardRepository extends JpaRepository<AuthorAward, String> {
}
