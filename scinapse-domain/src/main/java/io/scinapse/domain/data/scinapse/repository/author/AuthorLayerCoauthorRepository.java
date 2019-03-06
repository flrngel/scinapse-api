package io.scinapse.domain.data.scinapse.repository.author;

import io.scinapse.domain.data.scinapse.model.author.AuthorLayerCoauthor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorLayerCoauthorRepository extends JpaRepository<AuthorLayerCoauthor, AuthorLayerCoauthor.AuthorLayerCoauthorId> {
    void deleteByIdAuthorId(long authorId);
}
