package network.pluto.absolute.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.RequiredArgsConstructor;
import network.pluto.bibliotheca.models.Collection;
import network.pluto.bibliotheca.models.CollectionPaper;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.repositories.CollectionPaperRepository;
import network.pluto.bibliotheca.repositories.CollectionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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

    public Collection find(long collectionId) {
        return collectionRepository.findOne(collectionId);
    }

    public List<Collection> findIn(List<Long> collectionIds) {
        return collectionRepository.findByIdIn(collectionIds);
    }

    public Page<Collection> findByCreator(Member creator, Pageable pageable) {
        return collectionRepository.findByCreatedByOrderByCreatedAtDesc(creator, pageable);
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

    @Transactional
    public void addPaper(List<CollectionPaper> collectionPapers) {
        collectionPapers.forEach(this::addPaper);
    }

    public List<CollectionPaper> findByCollectionId(long collectionId) {
        return collectionPaperRepository.findByIdCollectionId(collectionId);
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
    }

}
