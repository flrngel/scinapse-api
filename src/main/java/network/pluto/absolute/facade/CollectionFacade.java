package network.pluto.absolute.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.CollectionDto;
import network.pluto.absolute.dto.CollectionPaperDto;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.dto.collection.CollectionPaperAddRequest;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.CollectionService;
import network.pluto.absolute.service.MemberService;
import network.pluto.absolute.service.mag.PaperService;
import network.pluto.bibliotheca.models.Collection;
import network.pluto.bibliotheca.models.CollectionPaper;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.mag.Paper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional(readOnly = true)
@Component
@RequiredArgsConstructor
public class CollectionFacade {

    private final CollectionService collectionService;
    private final MemberService memberService;
    private final PaperFacade paperFacade;
    private final PaperService paperService;

    @Transactional
    public CollectionDto create(JwtUser user, CollectionDto dto) {
        Member member = memberService.getMember(user.getId());

        Collection entity = dto.toEntity();
        entity.setCreatedBy(member);

        Collection created = collectionService.create(entity);
        return CollectionDto.of(created);
    }

    public CollectionDto find(long collectionId) {
        Collection one = collectionService.find(collectionId);
        if (one == null) {
            throw new ResourceNotFoundException("Collection not found : " + collectionId);
        }

        return CollectionDto.of(one);
    }

    public Page<CollectionDto> findByCreator(long creatorId, Pageable pageable) {
        Member creator = memberService.findMember(creatorId);
        if (creator == null) {
            throw new ResourceNotFoundException("Member not found : " + creatorId);
        }

        return collectionService.findByCreator(creator, pageable).map(CollectionDto::of);
    }

    @Transactional
    public CollectionDto update(JwtUser user, long collectionId, CollectionDto dto) {
        Collection one = collectionService.find(collectionId);
        if (one == null) {
            throw new ResourceNotFoundException("Collection not found : " + collectionId);
        }

        if (one.getCreatedBy().getId() != user.getId()) {
            throw new AuthorizationServiceException("Updating collection is only possible by its creator");
        }

        Collection updated = collectionService.update(one, dto.toEntity());
        return CollectionDto.of(updated);
    }

    @Transactional
    public void delete(JwtUser user, long collectionId) {
        Collection one = collectionService.find(collectionId);
        if (one == null) {
            throw new ResourceNotFoundException("Collection not found : " + collectionId);
        }

        if (one.getCreatedBy().getId() != user.getId()) {
            throw new AuthorizationServiceException("Deleting collection is only possible by its creator");
        }

        collectionService.delete(one);
    }

    public List<CollectionPaperDto> getPapers(long collectionId) {
        Collection one = collectionService.find(collectionId);
        if (one == null) {
            throw new ResourceNotFoundException("Collection not found : " + collectionId);
        }

        List<CollectionPaper> papers = collectionService.findByCollectionId(collectionId);
        List<CollectionPaperDto> paperDtos = papers.stream().map(CollectionPaperDto::of).collect(Collectors.toList());

        List<Long> paperIds = paperDtos.stream().map(CollectionPaperDto::getPaperId).collect(Collectors.toList());
        Map<Long, Paper> map = paperService.findByIdIn(paperIds, false)
                .stream()
                .collect(Collectors.toMap(
                        Paper::getId,
                        Function.identity()
                ));

        paperDtos.forEach(cp -> {
            Paper paper = map.get(cp.getPaperId());
            if (paper == null) {
                return;
            }

            // set paper dto
            cp.setPaper(PaperDto.detail(paper));
        });

        return paperDtos;
    }

    @Transactional
    public void addPaper(JwtUser user, CollectionPaperAddRequest request) {
        List<Collection> collections = collectionService.findIn(request.getCollectionIds());
        if (collections.isEmpty() || collections.size() != request.getCollectionIds().size()) {
            ArrayList<Long> collectionIds = new ArrayList<>(request.getCollectionIds());
            collectionIds.removeAll(collections.stream().map(Collection::getId).collect(Collectors.toList()));
            throw new ResourceNotFoundException("Collection not found : " + collectionIds);
        }

        List<Long> invalidIds = collections.stream().filter(c -> c.getCreatedBy().getId() != user.getId()).map(Collection::getId).collect(Collectors.toList());
        if (!invalidIds.isEmpty()) {
            throw new AuthorizationServiceException("Updating collection is only possible by its creator : " + invalidIds);
        }

        collectionService.addPaper(request.toEntities());
    }

    @Transactional
    public void delete(JwtUser user, long collectionId, List<Long> paperIds) {
        Collection one = collectionService.find(collectionId);
        if (one == null) {
            throw new ResourceNotFoundException("Collection not found : " + collectionId);
        }

        if (one.getCreatedBy().getId() != user.getId()) {
            throw new AuthorizationServiceException("Deleting collection paper is only possible by its creator");
        }

        collectionService.delete(collectionId, paperIds);
    }

    @Transactional
    public CollectionPaperDto updateCollectionPaperNote(JwtUser user, long collectionId, long paperId, String newNote) {
        Collection one = collectionService.find(collectionId);
        if (one == null) {
            throw new ResourceNotFoundException("Collection not found : " + collectionId);
        }

        if (one.getCreatedBy().getId() != user.getId()) {
            throw new AuthorizationServiceException("Deleting collection paper is only possible by its creator");
        }

        CollectionPaper collectionPaper = collectionService.findCollectionPaper(collectionId, paperId);
        if (collectionPaper == null) {
            throw new ResourceNotFoundException("Collection paper not found : collection id - " + collectionId + ", paper id - " + paperId);
        }

        CollectionPaper updated = collectionService.updateCollectionPaperNote(collectionPaper, newNote);
        return CollectionPaperDto.of(updated);
    }

}
