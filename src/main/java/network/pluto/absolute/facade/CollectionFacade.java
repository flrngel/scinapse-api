package network.pluto.absolute.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.dto.collection.CollectionDto;
import network.pluto.absolute.dto.collection.CollectionPaperDto;
import network.pluto.absolute.dto.collection.MyCollectionDto;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.model.Collection;
import network.pluto.absolute.model.CollectionPaper;
import network.pluto.absolute.model.Member;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.CollectionService;
import network.pluto.absolute.service.MemberService;
import network.pluto.absolute.service.mag.PaperService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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
        if (member == null) {
            throw new ResourceNotFoundException("Member not found : " + member.getId());
        }

        long count = collectionService.collectionCount(member);
        if (count >= 50) {
            throw new BadRequestException("Each member can create up to 50 collections");
        }

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

    public Page<MyCollectionDto> findMyCollection(JwtUser user, Long paperId, Pageable pageable) {
        Member member = memberService.findMember(user.getId());
        if (member == null) {
            throw new ResourceNotFoundException("Member not found : " + user.getId());
        }

        Page<Collection> collections = collectionService.findByCreator(member, pageable);
        if (collections.getTotalElements() == 0) {
            // Member doesn't have any collections. Create default collection.
            Collection collection = collectionService.createDefault(member);
            PageRequest defaultPageable = new PageRequest(0, 10);
            collections = new PageImpl<>(Collections.singletonList(collection), defaultPageable, 1);
        }

        if (paperId == null) {
            return collections.map(MyCollectionDto::of);
        }

        List<Long> collectionIds = collections.getContent().stream().map(Collection::getId).collect(Collectors.toList());
        Map<Long, CollectionPaper> collectionPaperMap = collectionService.findByIds(collectionIds, paperId)
                .stream()
                .collect(Collectors.toMap(
                        cp -> cp.getId().getCollectionId(),
                        Function.identity()
                ));

        return collections.map(c -> MyCollectionDto.of(c, collectionPaperMap.containsKey(c.getId())));
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
        Map<Long, PaperDto> map = paperFacade.findIn(paperIds, PaperDto.compact())
                .stream()
                .collect(Collectors.toMap(
                        PaperDto::getId,
                        Function.identity()
                ));

        paperDtos.forEach(cp -> {
            PaperDto paper = map.get(cp.getPaperId());
            if (paper == null) {
                return;
            }

            // set paper dto
            cp.setPaper(paper);
        });

        return paperDtos;
    }

    @Transactional
    public void addPaper(JwtUser user, CollectionPaperDto request) {
        if (!paperService.exists(request.getPaperId())) {
            throw new ResourceNotFoundException("Paper not found : " + request.getPaperId());
        }

        Collection one = collectionService.find(request.getCollectionId());
        if (one == null) {
            throw new ResourceNotFoundException("Collection not found : " + request.getCollectionId());
        }

        if (one.getPaperCount() >= 100) {
            throw new BadRequestException("You can only add up to 100 papers to a collection");
        }

        if (one.getCreatedBy().getId() != user.getId()) {
            throw new AuthorizationServiceException("Deleting collection paper is only possible by its creator");
        }

        collectionService.addPaper(request.toEntity());
    }

    @Transactional
    public CollectionPaperDto updateCollectionPaperNote(JwtUser user, long collectionId, long paperId, String newNote) {
        Collection one = collectionService.find(collectionId);
        if (one == null) {
            throw new ResourceNotFoundException("Collection not found : " + collectionId);
        }

        if (one.getCreatedBy().getId() != user.getId()) {
            throw new AuthorizationServiceException("Adding collection paper is only possible by its creator");
        }

        CollectionPaper collectionPaper = collectionService.findCollectionPaper(collectionId, paperId);
        if (collectionPaper == null) {
            throw new ResourceNotFoundException("Collection paper not found : collection id - " + collectionId + ", paper id - " + paperId);
        }

        CollectionPaper updated = collectionService.updateCollectionPaperNote(collectionPaper, newNote);
        return CollectionPaperDto.of(updated);
    }

    @Transactional
    public void deletePapers(JwtUser user, long collectionId, List<Long> paperIds) {
        Collection one = collectionService.find(collectionId);
        if (one == null) {
            throw new ResourceNotFoundException("Collection not found : " + collectionId);
        }

        if (one.getCreatedBy().getId() != user.getId()) {
            throw new AuthorizationServiceException("Deleting collection paper is only possible by its creator");
        }

        collectionService.delete(collectionId, paperIds);
    }

}
