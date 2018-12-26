package io.scinapse.api.data.scinapse.repository.author;

import io.scinapse.api.data.scinapse.model.author.AuthorEducation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorEducationRepository extends JpaRepository<AuthorEducation, String> {
}
