package io.scinapse.domain.data.scinapse.repository.author;

import io.scinapse.domain.data.scinapse.model.author.AuthorLayerPaperHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorLayerPaperHistoryRepository extends JpaRepository<AuthorLayerPaperHistory, String> {
    void deleteByAuthorId(long authorId);
}
