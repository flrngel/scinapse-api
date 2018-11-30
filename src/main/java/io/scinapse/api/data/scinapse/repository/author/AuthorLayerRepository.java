package io.scinapse.api.data.scinapse.repository.author;

import io.scinapse.api.data.scinapse.model.author.AuthorLayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface AuthorLayerRepository extends JpaRepository<AuthorLayer, Long> {
    List<AuthorLayer> findByAuthorIdIn(Set<Long> authorIds);
}
