package io.scinapse.api.data.scinapse.repository.author;

import io.scinapse.api.data.scinapse.model.author.AuthorLayerFos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorLayerFosRepository extends JpaRepository<AuthorLayerFos, AuthorLayerFos.AuthorLayerFosId>, AuthorLayerFosRepositoryCustom {
    void deleteByIdAuthorId(long authorId);
}
