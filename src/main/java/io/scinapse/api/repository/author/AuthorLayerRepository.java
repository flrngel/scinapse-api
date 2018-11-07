package io.scinapse.api.repository.author;

import io.scinapse.api.model.author.AuthorLayer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorLayerRepository extends JpaRepository<AuthorLayer, Long> {
}
