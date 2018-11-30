package io.scinapse.api.service.author;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.data.academic.FieldsOfStudy;
import io.scinapse.api.data.academic.repository.*;
import io.scinapse.api.data.scinapse.model.Member;
import io.scinapse.api.data.scinapse.model.author.AuthorLayer;
import io.scinapse.api.data.scinapse.model.author.AuthorLayerFos;
import io.scinapse.api.data.scinapse.model.author.AuthorLayerPaper;
import io.scinapse.api.data.scinapse.model.author.AuthorLayerPaperHistory;
import io.scinapse.api.data.scinapse.repository.MemberRepository;
import io.scinapse.api.data.scinapse.repository.author.AuthorLayerFosRepository;
import io.scinapse.api.data.scinapse.repository.author.AuthorLayerPaperHistoryRepository;
import io.scinapse.api.data.scinapse.repository.author.AuthorLayerPaperRepository;
import io.scinapse.api.data.scinapse.repository.author.AuthorLayerRepository;
import io.scinapse.api.dto.mag.*;
import io.scinapse.api.enums.PaperSort;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.util.IdUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AuthorLayerService {

    private final AuthorLayerRepository authorLayerRepository;
    private final AuthorLayerPaperRepository authorLayerPaperRepository;
    private final AuthorLayerPaperHistoryRepository authorLayerPaperHistoryRepository;
    private final AuthorLayerFosRepository authorLayerFosRepository;
    private final AuthorRepository authorRepository;
    private final MemberRepository memberRepository;
    private final PaperAuthorRepository paperAuthorRepository;
    private final PaperRepository paperRepository;
    private final FieldsOfStudyRepository fieldsOfStudyRepository;
    private final AffiliationRepository affiliationRepository;

    @Transactional
    public void connect(Member member, AuthorDto author, AuthorLayerUpdateDto dto) {
        Optional.ofNullable(member.getAuthorId())
                .ifPresent((connectedAuthorId) -> {
                    throw new BadRequestException("The member[" + member.getId() + "] is already connected to the other author[" + connectedAuthorId + "]");
                });

        Optional.ofNullable(memberRepository.findByAuthorId(author.getId()))
                .ifPresent((connectedMember) -> {
                    throw new BadRequestException("The author[" + author.getId() + "] is already connected to the other member[" + connectedMember.getId() + "]");
                });

        Optional.ofNullable(authorLayerRepository.findOne(author.getId()))
                .ifPresent(layer -> {
                    throw new BadRequestException("The author[" + author.getId() + "] has layered information. It is not allowed to connect.");
                });

        // connect member with author.
        member.setAuthorId(author.getId());

        // the author does not have a layer. Let's create new one.
        createLayer(author, dto);
    }

    @Transactional
    public void disconnect(long authorId) {
        Optional.ofNullable(memberRepository.findByAuthorId(authorId)).ifPresent(member -> member.setAuthorId(null));
        authorLayerFosRepository.deleteByIdAuthorId(authorId);
        authorLayerPaperHistoryRepository.deleteByAuthorId(authorId);
        authorLayerPaperRepository.deleteByIdAuthorId(authorId);
        authorLayerRepository.delete(authorId);
    }

    private void createLayer(AuthorDto author, AuthorLayerUpdateDto dto) {
        AuthorLayer authorLayer = new AuthorLayer();

        // from author
        authorLayer.setAuthorId(author.getId());
        authorLayer.setName(author.getName());
        authorLayer.setPaperCount(author.getPaperCount());
        authorLayer.setCitationCount(author.getCitationCount());

        Optional.ofNullable(author.getLastKnownAffiliation())
                .ifPresent(aff -> authorLayer.setLastKnownAffiliationId(aff.getId()));

        AuthorLayer saved = authorLayerRepository.saveAndFlush(authorLayer);

        copyPapers(author, saved);
        initFos(saved);

        update(saved, dto);
    }

    private void copyPapers(AuthorDto author, AuthorLayer layer) {
        List<AuthorLayerPaper> layerPapers = paperAuthorRepository.findByIdAuthorId(author.getId())
                .stream()
                .map(paperAuthor -> {
                    AuthorLayerPaper layerPaper = new AuthorLayerPaper(layer.getAuthorId(), paperAuthor.getId().getPaperId());
                    Optional.ofNullable(paperAuthor.getAffiliation())
                            .ifPresent(aff -> layerPaper.setAffiliationId(aff.getId()));
                    layerPaper.setAuthorSequenceNumber(paperAuthor.getAuthorSequenceNumber());
                    return layerPaper;
                })
                .collect(Collectors.toList());
        authorLayerPaperRepository.save(layerPapers);
    }

    private void initFos(AuthorLayer layer) {
        List<Long> relatedFosIds = authorRepository.getRelatedFos(layer.getAuthorId());
        List<FieldsOfStudy> relatedFos = fieldsOfStudyRepository.findByIdIn(relatedFosIds);
        List<AuthorLayerFos> fosList = relatedFos
                .stream()
                .map(fos -> new AuthorLayerFos(layer, fos))
                .collect(Collectors.toList());

        List<AuthorLayerFos> saved = authorLayerFosRepository.save(fosList);
        layer.setFosList(saved);
    }

    public boolean exists(long authorId) {
        return authorLayerRepository.exists(authorId);
    }

    public Optional<AuthorLayer> find(long authorId) {
        return Optional.ofNullable(authorLayerRepository.findOne(authorId));
    }

    public Page<AuthorLayerPaper> findPapers(long authorId, String[] keywords, PageRequest pageRequest) {
        AuthorLayer author = find(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found: " + authorId));

        PaperSort sort = PaperSort.find(pageRequest.getSort());
        List<AuthorLayerPaper> papers = authorLayerPaperRepository.findPapers(authorId, false, keywords, sort, pageRequest.toPageable());
        return new PageImpl<>(papers, pageRequest.toPageable(), author.getPaperCount());
    }

    @Transactional
    public void removePapers(AuthorLayer layer, Set<Long> paperIds) {
        List<AuthorLayerPaper> layerPapers = authorLayerPaperRepository.findByIdAuthorIdAndIdPaperIdIn(layer.getAuthorId(), paperIds);
        if (CollectionUtils.isEmpty(layerPapers)) {
            return;
        }

        // set layer paper's status
        layerPapers
                .forEach((paper) -> paper.setStatus(AuthorLayerPaper.PaperStatus.PENDING_REMOVE));

        // log action history
        List<AuthorLayerPaperHistory> histories = layerPapers
                .stream()
                .map((paper) -> {
                    AuthorLayerPaperHistory history = new AuthorLayerPaperHistory();
                    history.setId(IdUtils.generateStringId(authorLayerPaperHistoryRepository));
                    history.setAction(AuthorLayerPaperHistory.PaperAction.REMOVE);
                    history.setStatus(AuthorLayerPaperHistory.ActionStatus.PENDING);
                    history.setAuthorId(layer.getAuthorId());
                    history.setPaperId(paper.getId().getPaperId());
                    return history;
                })
                .collect(Collectors.toList());
        authorLayerPaperHistoryRepository.save(histories);

        // set layer's status
        layer.setStatus(AuthorLayer.LayerStatus.PENDING);
        layer.setPaperStatus(AuthorLayer.LayerStatus.PENDING);
        layer.setPaperCount(authorLayerPaperRepository.getPaperCount(layer.getAuthorId()));
    }

    @Transactional
    public void addPapers(AuthorLayer layer, Set<Long> paperIds) {
        List<Long> existingLayerPaperIds = authorLayerPaperRepository.findByIdAuthorIdAndIdPaperIdIn(layer.getAuthorId(), paperIds)
                .stream()
                .filter(lp -> lp.getStatus() != AuthorLayerPaper.PaperStatus.PENDING_REMOVE)
                .map(lp -> lp.getId().getPaperId())
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(existingLayerPaperIds)) {
            throw new BadRequestException("Duplicate registration is not allowed. Duplicated: " + existingLayerPaperIds);
        }

        // add author layer papers
        List<Long> existingPaperIds = paperRepository.findIdByIdIn(paperIds);
        List<AuthorLayerPaper> layerPapers = existingPaperIds
                .stream()
                .map(paperId -> {
                    AuthorLayerPaper layerPaper = new AuthorLayerPaper(layer.getAuthorId(), paperId);
                    layerPaper.setStatus(AuthorLayerPaper.PaperStatus.PENDING_ADD);
                    return layerPaper;
                })
                .collect(Collectors.toList());
        authorLayerPaperRepository.save(layerPapers);

        // log action history
        List<AuthorLayerPaperHistory> histories = layerPapers
                .stream()
                .map((paper) -> {
                    AuthorLayerPaperHistory history = new AuthorLayerPaperHistory();
                    history.setId(IdUtils.generateStringId(authorLayerPaperHistoryRepository));
                    history.setAction(AuthorLayerPaperHistory.PaperAction.ADD);
                    history.setStatus(AuthorLayerPaperHistory.ActionStatus.PENDING);
                    history.setAuthorId(layer.getAuthorId());
                    history.setPaperId(paper.getId().getPaperId());
                    return history;
                })
                .collect(Collectors.toList());
        authorLayerPaperHistoryRepository.save(histories);

        // set layer's status
        layer.setStatus(AuthorLayer.LayerStatus.PENDING);
        layer.setPaperStatus(AuthorLayer.LayerStatus.PENDING);
        layer.setPaperCount(authorLayerPaperRepository.getPaperCount(layer.getAuthorId()));
    }

    @Transactional
    public List<AuthorLayerPaper> updateSelected(AuthorLayer layer, Set<Long> selectedPaperIds) {
        if (selectedPaperIds.size() > 10) {
            throw new BadRequestException("Author can select up to 10 papers as selected. Current size: " + selectedPaperIds.size());
        }

        List<AuthorLayerPaper> layerPapersToSelect = authorLayerPaperRepository.findByIdAuthorIdAndIdPaperIdIn(layer.getAuthorId(), selectedPaperIds);

        List<Long> removedPaperIds = layerPapersToSelect
                .stream()
                .filter(lp -> lp.getStatus() == AuthorLayerPaper.PaperStatus.PENDING_REMOVE)
                .map(lp -> lp.getId().getPaperId())
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(removedPaperIds)) {
            throw new BadRequestException("Cannot include papers to be removed as a selected publication: " + removedPaperIds);
        }

        // unmark existing selected publications
        authorLayerPaperRepository.findSelectedPapers(layer.getAuthorId())
                .forEach(lp -> lp.setSelected(false));

        // mark newly selected publications
        layerPapersToSelect
                .forEach(lp -> lp.setSelected(true));

        return layerPapersToSelect;
    }

    public List<AuthorLayerPaper> findSelectedPapers(long authorId) {
        return authorLayerPaperRepository.findSelectedPapers(authorId);
    }

    @Transactional
    public AuthorLayer update(AuthorLayer layer, AuthorLayerUpdateDto updateDto) {
        boolean isUpdated = false;

        if (!StringUtils.equals(layer.getBio(), updateDto.getBio())) {
            isUpdated = true;
            layer.setBio(updateDto.getBio());
        }

        if (!StringUtils.equals(layer.getName(), updateDto.getName())) {
            isUpdated = true;
            layer.setName(updateDto.getName());
        }

        if (!StringUtils.equals(layer.getEmail(), updateDto.getEmail())) {
            isUpdated = true;
            layer.setEmail(updateDto.getEmail());
        }

        if (!StringUtils.equals(layer.getWebPage(), updateDto.getWebPage())) {
            isUpdated = true;
            layer.setWebPage(updateDto.getWebPage());
        }

        Long updateAffiliationId = updateDto.getAffiliationId();
        Long oldAffiliationId = layer.getLastKnownAffiliationId();
        if (updateAffiliationId == null) {
            if (oldAffiliationId != null) { // case : user removes last known affiliation
                isUpdated = true;
                layer.setLastKnownAffiliationId(null);
            }
        } else {
            if (updateAffiliationId <= 0) {
                throw new BadRequestException("Cannot update affiliation with invalid affiliation ID: " + updateAffiliationId);
            }
            if (!updateAffiliationId.equals(oldAffiliationId)) { // case : user updates last known affiliation
                if (!affiliationRepository.exists(updateAffiliationId)) {
                    throw new BadRequestException("Cannot update affiliation with invalid affiliation ID: " + updateAffiliationId);
                }
                isUpdated = true;
                layer.setLastKnownAffiliationId(updateAffiliationId);
            }
        }

        if (isUpdated) {
            layer.setStatus(AuthorLayer.LayerStatus.PENDING);
        }

        return layer;
    }

    public List<AuthorLayerPaper> getAllLayerPapers(long authorId) {
        return authorLayerPaperRepository.findAllLayerPapers(authorId);
    }

    public void decoratePaperAuthors(List<PaperAuthorDto> dtos) {
        Set<Long> authorIds = dtos.stream()
                .map(PaperAuthorDto::getId)
                .collect(Collectors.toSet());

        Map<Long, AuthorLayer> layerMap = authorLayerRepository.findByAuthorIdIn(authorIds)
                .stream()
                .collect(Collectors.toMap(
                        AuthorLayer::getAuthorId,
                        Function.identity()
                ));

        dtos
                .forEach(dto -> {
                    AuthorLayer layer = layerMap.get(dto.getId());
                    if (layer == null) {
                        return;
                    }
                    dto.setLayered(true);
                    dto.setName(layer.getName());
                });
    }

    public List<AuthorDto> decorateAuthors(List<AuthorDto> dtos) {
        Map<Long, AuthorDto> authorMap = dtos.stream()
                .collect(Collectors.toMap(
                        AuthorDto::getId,
                        Function.identity()
                ));

        authorLayerRepository.findByAuthorIdIn(authorMap.keySet())
                .forEach(layer -> {
                    AuthorDto dto = authorMap.get(layer.getAuthorId());
                    if (dto == null) {
                        return;
                    }
                    dto.setLayered(true);
                    dto.setName(layer.getName());
                });

        return dtos;
    }

    public void decorateAuthorDetail(AuthorDto dto, AuthorLayer layer) {
        dto.setLayered(true);
        dto.setName(layer.getName());
        dto.setEmail(layer.getEmail());
        dto.setPaperCount(layer.getPaperCount());
        dto.setBio(layer.getBio());
        dto.setWebPage(layer.getWebPage());

        Optional.ofNullable(layer.getLastKnownAffiliationId())
                .map(affiliationRepository::findOne)
                .map(AffiliationDto::new)
                .ifPresent(dto::setLastKnownAffiliation);

        if (!CollectionUtils.isEmpty(layer.getFosList())) {
            List<Long> fosIds = layer.getFosList()
                    .stream()
                    .map(AuthorLayerFos::getId)
                    .map(AuthorLayerFos.AuthorLayerFosId::getFosId)
                    .collect(Collectors.toList());

            Map<Long, FosDto> fosMap = fieldsOfStudyRepository.findByIdIn(fosIds)
                    .stream()
                    .map(FosDto::new)
                    .collect(Collectors.toMap(
                            FosDto::getId,
                            Function.identity()
                    ));

            List<FosDto> fosDtos = fosIds
                    .stream()
                    .map(fosMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            dto.setFosList(fosDtos);
        }
    }

    public List<AuthorSearchPaperDto> decorateSearchResult(long authorId, List<PaperDto> paperDtos) {
        Set<Long> paperIds = paperDtos.stream().map(PaperDto::getId).collect(Collectors.toSet());
        Map<Long, AuthorLayerPaper> layerPaperMap = authorLayerPaperRepository.findByIdAuthorIdAndIdPaperIdIn(authorId, paperIds)
                .stream()
                .filter(lp -> lp.getStatus() != AuthorLayerPaper.PaperStatus.PENDING_REMOVE)
                .collect(Collectors.toMap(
                        lp -> lp.getId().getPaperId(),
                        Function.identity()
                ));

        return paperDtos
                .stream()
                .map(paperDto -> {
                    boolean included = Optional.ofNullable(layerPaperMap.get(paperDto.getId())).isPresent();
                    return new AuthorSearchPaperDto(paperDto, included);
                })
                .collect(Collectors.toList());
    }

}
