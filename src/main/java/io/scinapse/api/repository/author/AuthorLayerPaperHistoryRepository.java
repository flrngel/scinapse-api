package io.scinapse.api.repository.author;

import io.scinapse.api.model.author.AuthorLayerPaperHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorLayerPaperHistoryRepository extends JpaRepository<AuthorLayerPaperHistory, String> {
}
