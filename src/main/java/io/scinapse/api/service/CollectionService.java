package io.scinapse.api.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.data.scinapse.model.Collection;
import io.scinapse.api.data.scinapse.model.CollectionPaper;
import io.scinapse.api.data.scinapse.model.Member;
import io.scinapse.api.data.scinapse.repository.CollectionPaperRepository;
import io.scinapse.api.data.scinapse.repository.CollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@XRayEnabled
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionPaperRepository collectionPaperRepository;

    @Transactional
    public Collection create(Collection dto) {
        return collectionRepository.save(dto);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Collection createDefault(Member member) {
        Collection collection = new Collection();
        collection.setCreatedBy(member);
        collection.setTitle(member.getFullName() + "'s Collection");
        collection.setDefault(true);
        return collectionRepository.save(collection);
    }

    public Collection find(long collectionId) {
        return collectionRepository.findOne(collectionId);
    }

    public Page<Collection> findByCreator(Member creator, Pageable pageable) {
        return collectionRepository.findByCreatedByOrderByUpdatedAtDesc(creator, pageable);
    }

    public long collectionCount(Member creator) {
        return collectionRepository.countByCreatedBy(creator);
    }

    @Transactional
    public Collection update(Collection old, Collection updateTo) {
        old.setTitle(updateTo.getTitle());
        old.setDescription(updateTo.getDescription());
        return old;
    }

    @Transactional
    public void delete(Collection collection) {
        // delete every papers in collection
        collectionPaperRepository.deleteByIdCollectionId(collection.getId());
        collectionRepository.delete(collection);
    }

    @Transactional
    public void addPaper(CollectionPaper collectionPaper) {
        collectionPaperRepository.save(collectionPaper);

        long collectionId = collectionPaper.getId().getCollectionId();
        Collection collection = collectionRepository.findOne(collectionId);
        int paperCount = collectionPaperRepository.countByIdCollectionId(collectionId);
        collection.setPaperCount(paperCount);
    }

    public List<CollectionPaper> findByCollectionId(long collectionId) {
        return collectionPaperRepository.findByIdCollectionIdOrderByUpdatedAtDesc(collectionId);
    }

    public List<CollectionPaper> findByIds(List<Long> collectionIds, long paperId) {
        return collectionPaperRepository.findByIdCollectionIdInAndIdPaperId(collectionIds, paperId);
    }

    @Transactional
    public CollectionPaper update(CollectionPaper old, CollectionPaper updateTo) {
        old.setNote(updateTo.getNote());
        return old;
    }

    public CollectionPaper findCollectionPaper(long collectionId, long paperId) {
        CollectionPaper.CollectionPaperId id = CollectionPaper.CollectionPaperId.of(collectionId, paperId);
        return collectionPaperRepository.findOne(id);
    }

    @Transactional
    public CollectionPaper updateCollectionPaperNote(CollectionPaper collectionPaper, String newNote) {
        collectionPaper.setNote(newNote);
        return collectionPaper;
    }

    @Transactional
    public void delete(long collectionId, List<Long> paperIds) {
        collectionPaperRepository.deleteByIdCollectionIdAndIdPaperIdIn(collectionId, paperIds);

        Collection collection = collectionRepository.findOne(collectionId);
        int paperCount = collectionPaperRepository.countByIdCollectionId(collectionId);
        collection.setPaperCount(paperCount);
    }

}
