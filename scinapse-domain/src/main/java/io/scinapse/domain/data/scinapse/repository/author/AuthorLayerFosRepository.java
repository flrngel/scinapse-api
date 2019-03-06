package io.scinapse.domain.data.scinapse.repository.author;

import io.scinapse.domain.data.scinapse.model.author.AuthorLayerFos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface AuthorLayerFosRepository extends JpaRepository<AuthorLayerFos, AuthorLayerFos.AuthorLayerFosId>, AuthorLayerFosRepositoryCustom {
    void deleteByIdAuthorId(long authorId);
    List<AuthorLayerFos> findByIdAuthorIdIn(Set<Long> authorIds);
}
