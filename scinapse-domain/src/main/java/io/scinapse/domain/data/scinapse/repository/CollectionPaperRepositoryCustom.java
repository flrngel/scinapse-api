package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.data.scinapse.model.CollectionPaper;
import io.scinapse.domain.enums.PaperSort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface CollectionPaperRepositoryCustom {
    Page<CollectionPaper> findPapers(@Param("collectionId") long collectionId, String[] keywords, PaperSort sort, Pageable pageable);
}
