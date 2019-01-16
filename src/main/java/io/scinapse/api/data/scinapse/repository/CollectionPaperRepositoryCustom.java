package io.scinapse.api.data.scinapse.repository;

import io.scinapse.api.data.scinapse.model.CollectionPaper;
import io.scinapse.api.enums.PaperSort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface CollectionPaperRepositoryCustom {
    Page<CollectionPaper> findPapers(@Param("collectionId") long collectionId, String[] keywords, PaperSort sort, Pageable pageable);
}
