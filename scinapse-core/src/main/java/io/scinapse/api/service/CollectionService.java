package io.scinapse.api.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.academic.dto.AcPaperDto;
import io.scinapse.domain.data.scinapse.model.Collection;
import io.scinapse.domain.data.scinapse.model.CollectionPaper;
import io.scinapse.domain.data.scinapse.model.Member;
import io.scinapse.domain.data.scinapse.repository.CollectionPaperRepository;
import io.scinapse.domain.data.scinapse.repository.CollectionRepository;
import io.scinapse.api.dto.v2.PaperItemDto;
import io.scinapse.domain.dto.CollectionWrapper;
import io.scinapse.domain.enums.PaperSort;
import io.scinapse.api.security.jwt.JwtUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
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

    public Page<CollectionPaper> findPapers(long collectionId, String[] keywords, PaperSort sort, Pageable pageable) {
        return collectionPaperRepository.findPapers(collectionId, keywords, sort, pageable);
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

    public void decoratePaperItems(List<PaperItemDto> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return;
        }

        if (!(authentication instanceof JwtUser)) {
            // FIXME: maybe anonymous user
            return;
        }

        JwtUser user = (JwtUser) authentication;
        Set<Long> paperIds = dtos.stream().map(PaperItemDto::getOrigin).map(AcPaperDto::getId).collect(Collectors.toSet());

        Map<Long, List<CollectionWrapper>> collectionMap = collectionRepository.findBySavedPapers(user.getId(), paperIds);
        dtos.forEach(dto -> {
            long id = dto.getOrigin().getId();
            List<CollectionWrapper> collections = collectionMap.get(id);
            if (CollectionUtils.isEmpty(collections)) {
                return;
            }

            List<PaperItemDto.SavedInCollection> converted = collections
                    .stream()
                    .map(c -> {
                        PaperItemDto.SavedInCollection saved = new PaperItemDto.SavedInCollection();
                        saved.setId(c.getId());
                        saved.setTitle(c.getTitle());
                        saved.setUpdatedAt(c.getUpdatedAt());
                        return saved;
                    })
                    .collect(Collectors.toList());

            PaperItemDto.Relation relation = new PaperItemDto.Relation();
            relation.setSavedInCollections(converted);

            dto.setRelation(relation);
        });
    }

}
