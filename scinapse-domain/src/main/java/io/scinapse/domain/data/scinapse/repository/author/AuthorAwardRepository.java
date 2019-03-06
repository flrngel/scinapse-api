package io.scinapse.domain.data.scinapse.repository.author;

import io.scinapse.domain.data.scinapse.model.author.AuthorAward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorAwardRepository extends JpaRepository<AuthorAward, String> {
    void deleteByAuthorAuthorId(long authorId);
    List<AuthorAward> findByAuthorAuthorId(long authorId);
}
