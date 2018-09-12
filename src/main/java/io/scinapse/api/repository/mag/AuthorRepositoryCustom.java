package io.scinapse.api.repository.mag;

import io.scinapse.api.model.mag.Author;
import io.scinapse.api.model.mag.PaperAuthor;

import java.util.List;

public interface AuthorRepositoryCustom {
    List<PaperAuthor> getAuthorsByPaperIdIn(List<Long> paperIds);
    List<Author> findCoAuthors(long authorId);
}
