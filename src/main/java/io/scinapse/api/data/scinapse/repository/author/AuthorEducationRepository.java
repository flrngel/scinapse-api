package io.scinapse.api.data.scinapse.repository.author;

import io.scinapse.api.data.scinapse.model.author.AuthorEducation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorEducationRepository extends JpaRepository<AuthorEducation, String> {
    void deleteByAuthorAuthorId(long authorId);
    List<AuthorEducation> findByAuthorAuthorId(long authorId);
}
