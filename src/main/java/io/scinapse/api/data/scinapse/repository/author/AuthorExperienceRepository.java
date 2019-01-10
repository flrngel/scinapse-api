package io.scinapse.api.data.scinapse.repository.author;

import io.scinapse.api.data.scinapse.model.author.AuthorExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorExperienceRepository extends JpaRepository<AuthorExperience, String> {
    void deleteByAuthorAuthorId(long authorId);
    List<AuthorExperience> findByAuthorAuthorId(long authorId);
}
