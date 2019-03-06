package io.scinapse.domain.data.scinapse.repository.author;

import io.scinapse.domain.data.scinapse.model.author.AuthorLayerPaper;
import io.scinapse.domain.enums.PaperSort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface AuthorLayerPaperRepositoryCustom {
    Page<AuthorLayerPaper> findPapers(@Param("authorId") long authorId, boolean showAll, String[] keywords, PaperSort sort, Pageable pageable);
}
