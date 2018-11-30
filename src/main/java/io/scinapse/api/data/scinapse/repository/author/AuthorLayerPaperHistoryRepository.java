package io.scinapse.api.data.scinapse.repository.author;

import io.scinapse.api.data.scinapse.model.author.AuthorLayerPaperHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorLayerPaperHistoryRepository extends JpaRepository<AuthorLayerPaperHistory, String> {
    void deleteByAuthorId(long authorId);
}
