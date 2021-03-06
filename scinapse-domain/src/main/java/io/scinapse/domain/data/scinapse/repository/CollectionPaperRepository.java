package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.data.scinapse.model.CollectionPaper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionPaperRepository extends JpaRepository<CollectionPaper, CollectionPaper.CollectionPaperId>, CollectionPaperRepositoryCustom {
    List<CollectionPaper> findByIdCollectionIdOrderByUpdatedAtDesc(long collectionId);
    List<CollectionPaper> findByIdCollectionIdInAndIdPaperId(List<Long> collectionIds, long paperId);
    int countByIdCollectionId(long collectionId);
    void deleteByIdCollectionIdAndIdPaperIdIn(long collectionId, List<Long> paperIds);
    void deleteByIdCollectionId(long collectionId);
}
