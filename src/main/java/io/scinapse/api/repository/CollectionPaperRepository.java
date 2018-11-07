package io.scinapse.api.repository;

import io.scinapse.api.model.CollectionPaper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionPaperRepository extends JpaRepository<CollectionPaper, CollectionPaper.CollectionPaperId> {
    List<CollectionPaper> findByIdCollectionIdOrderByUpdatedAtDesc(long collectionId);
    List<CollectionPaper> findByIdCollectionIdInAndIdPaperId(List<Long> collectionIds, long paperId);
    int countByIdCollectionId(long collectionId);
    void deleteByIdCollectionIdAndIdPaperIdIn(long collectionId, List<Long> paperIds);
    void deleteByIdCollectionId(long collectionId);
}
