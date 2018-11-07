package io.scinapse.api.service.author;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.dto.mag.AuthorLayerUpdateDto;
import io.scinapse.api.enums.PaperSort;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.model.Member;
import io.scinapse.api.model.author.AuthorLayer;
import io.scinapse.api.model.author.AuthorLayerPaper;
import io.scinapse.api.model.author.AuthorLayerPaperHistory;
import io.scinapse.api.model.mag.Author;
import io.scinapse.api.model.mag.Paper;
import io.scinapse.api.repository.MemberRepository;
import io.scinapse.api.repository.author.AuthorLayerPaperHistoryRepository;
import io.scinapse.api.repository.author.AuthorLayerPaperRepository;
import io.scinapse.api.repository.author.AuthorLayerRepository;
import io.scinapse.api.repository.mag.PaperAuthorRepository;
import io.scinapse.api.repository.mag.PaperRepository;
import io.scinapse.api.util.IdUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AuthorLayerService {

    private final AuthorLayerRepository authorLayerRepository;
    private final AuthorLayerPaperRepository authorLayerPaperRepository;
    private final AuthorLayerPaperHistoryRepository authorLayerPaperHistoryRepository;
    private final MemberRepository memberRepository;
    private final PaperAuthorRepository paperAuthorRepository;
    private final PaperRepository paperRepository;

    @Transactional
    public void connect(Member member, Author author) {
        Optional.ofNullable(member.getAuthorId())
                .ifPresent((connectedAuthorId) -> {
                    throw new BadRequestException("The member[" + member.getId() + "] is already connected to the other author[" + connectedAuthorId + "]");
                });

        Optional.ofNullable(memberRepository.findByAuthorId(author.getId()))
                .ifPresent((connectedMember) -> {
                    throw new BadRequestException("The author[" + author.getId() + "] is already connected to the other member[" + connectedMember.getId() + "]");
                });

        // connect member with author.
        member.setAuthorId(author.getId());

        if (authorLayerRepository.exists(author.getId())) {
            // the author already has a layer which is probably created by admin users.
            // do not create duplicates.
            return;
        }

        // the author does not have a layer. Let's create new one.
        createLayer(author);
    }

    private void createLayer(Author author) {
        AuthorLayer authorLayer = new AuthorLayer();
        authorLayer.setAuthor(author);
        authorLayer.setAuthorId(author.getId());
        authorLayer.setName(author.getName());
        authorLayer.setLastKnownAffiliation(author.getLastKnownAffiliation());
        authorLayer.setPaperCount(author.getPaperCount());
        authorLayer.setCitationCount(author.getCitationCount());
        authorLayerRepository.save(authorLayer);

        copyPapers(author, authorLayer);
    }

    private void copyPapers(Author author, AuthorLayer authorLayer) {
        List<AuthorLayerPaper> layerPapers = paperAuthorRepository.findByIdAuthorId(author.getId())
                .stream()
                .map(paperAuthor -> {
                    AuthorLayerPaper layerPaper = new AuthorLayerPaper(authorLayer, paperAuthor.getPaper());
                    layerPaper.setAffiliation(paperAuthor.getAffiliation());
                    layerPaper.setAuthorSequenceNumber(paperAuthor.getAuthorSequenceNumber());
                    return layerPaper;
                })
                .collect(Collectors.toList());
        authorLayerPaperRepository.save(layerPapers);
    }

    public boolean exists(long authorId) {
        return authorLayerRepository.exists(authorId);
    }

    public Optional<AuthorLayer> find(long authorId) {
        return Optional.ofNullable(authorLayerRepository.findOne(authorId));
    }

    public Page<AuthorLayerPaper> getPapers(long authorId, PageRequest pageRequest) {
        AuthorLayer author = find(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found: " + authorId));

        List<AuthorLayerPaper> papers = getPaperList(authorId, pageRequest);
        return new PageImpl<>(papers, pageRequest.toPageable(), author.getPaperCount());
    }

    private List<AuthorLayerPaper> getPaperList(long authorId, PageRequest pageRequest) {
        PaperSort sort = PaperSort.find(pageRequest.getSort());
        if (sort == null) {
            return authorLayerPaperRepository.getMostCitations(authorId, pageRequest.toPageable());
        }

        switch (sort) {
            case NEWEST_FIRST:
                return authorLayerPaperRepository.getNewest(authorId, pageRequest.toPageable());
            case OLDEST_FIRST:
                return authorLayerPaperRepository.getOldest(authorId, pageRequest.toPageable());
            default:
                return authorLayerPaperRepository.getMostCitations(authorId, pageRequest.toPageable());
        }
    }

    @Transactional
    public void removePapers(AuthorLayer layer, List<Long> paperIds) {
        List<AuthorLayerPaper> layerPapers = authorLayerPaperRepository.findByIdAuthorIdAndIdPaperIdIn(layer.getAuthorId(), paperIds);
        if (CollectionUtils.isEmpty(layerPapers)) {
            return;
        }

        List<Long> selectedPaperIds = layerPapers.stream()
                .filter(AuthorLayerPaper::isSelected)
                .map(lp -> lp.getId().getPaperId())
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(selectedPaperIds)) {
            throw new BadRequestException("Cannot remove selected publications: " + selectedPaperIds);
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
                    history.setPaper(paper.getPaper());
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
    public void addPapers(AuthorLayer layer, List<Long> paperIds) {
        List<Long> existingLayerPaperIds = authorLayerPaperRepository.findByIdAuthorIdAndIdPaperIdIn(layer.getAuthorId(), paperIds)
                .stream()
                .map(lp -> lp.getId().getPaperId()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(existingLayerPaperIds)) {
            throw new BadRequestException("Duplicate registration is not allowed. Duplicated: " + existingLayerPaperIds);
        }

        // add author layer papers
        List<Paper> papers = paperRepository.findByIdIn(paperIds);
        List<AuthorLayerPaper> layerPapers = papers.stream()
                .map(p -> {
                    AuthorLayerPaper layerPaper = new AuthorLayerPaper(layer, p);
                    layerPaper.setAffiliation(layer.getLastKnownAffiliation());
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
                    history.setPaper(paper.getPaper());
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
    public List<AuthorLayerPaper> updateSelected(Author author, List<Long> selectedPaperIds) {
        if (author.getLayer() == null) {
            throw new BadRequestException("Author[" + author.getId() + "] does not have a layer.");
        }

        List<AuthorLayerPaper> layerPapers = authorLayerPaperRepository.findByIdAuthorIdAndIdPaperIdIn(author.getId(), selectedPaperIds);
        if (CollectionUtils.isEmpty(layerPapers)) {
            throw new BadRequestException("No papers to add selected publications. Please choose papers from your own papers");
        }

        List<Long> removedPaperIds = layerPapers.stream()
                .filter(lp -> lp.getStatus() == AuthorLayerPaper.PaperStatus.PENDING_REMOVE)
                .map(lp -> lp.getId().getPaperId())
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(removedPaperIds)) {
            throw new BadRequestException("Cannot include papers to be removed as a selected publication: " + removedPaperIds);
        }

        // unmark existing selected publications
        authorLayerPaperRepository.findSelectedPapers(author.getId())
                .forEach(lp -> lp.setSelected(false));

        // mark newly selected publications
        layerPapers
                .forEach(lp -> lp.setSelected(true));

        return layerPapers;
    }

    public List<AuthorLayerPaper> findSelectedPapers(long authorId) {
        return authorLayerPaperRepository.findSelectedPapers(authorId);
    }

    @Transactional
    public AuthorLayer update(AuthorLayer layer, AuthorLayerUpdateDto updateDto) {
        layer.setBio(updateDto.getBio());
        return layer;
    }

}
