package io.scinapse.domain.data.scinapse.repository.author;

import io.scinapse.domain.data.scinapse.model.author.AuthorExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorExperienceRepository extends JpaRepository<AuthorExperience, String> {
    void deleteByAuthorAuthorId(long authorId);
    List<AuthorExperience> findByAuthorAuthorId(long authorId);
}
