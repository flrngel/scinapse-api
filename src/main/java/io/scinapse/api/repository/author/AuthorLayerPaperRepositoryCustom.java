package io.scinapse.api.repository.author;

import io.scinapse.api.enums.PaperSort;
import io.scinapse.api.model.author.AuthorLayerPaper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuthorLayerPaperRepositoryCustom {
    List<AuthorLayerPaper> findPapers(@Param("authorId") long authorId, boolean showAll, String[] keywords, PaperSort sort, Pageable pageable);
}
