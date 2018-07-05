package io.scinapse.api.repository.mag;

import io.scinapse.api.model.mag.Author;
import io.scinapse.api.model.mag.PaperAuthorAffiliation;

import java.util.List;

public interface AuthorRepositoryCustom {
    List<PaperAuthorAffiliation> getAuthorsByPaperIdIn(List<Long> paperIds);
    List<Author> findCoAuthors(long authorId);
}
