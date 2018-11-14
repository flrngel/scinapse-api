package io.scinapse.api.service.author;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.dto.PaperTitleDto;
import io.scinapse.api.dto.mag.AuthorLayerUpdateDto;
import io.scinapse.api.enums.PaperSort;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.model.Member;
import io.scinapse.api.model.author.AuthorLayer;
import io.scinapse.api.model.author.AuthorLayerFos;
import io.scinapse.api.model.author.AuthorLayerPaper;
import io.scinapse.api.model.author.AuthorLayerPaperHistory;
import io.scinapse.api.model.mag.Affiliation;
import io.scinapse.api.model.mag.Author;
import io.scinapse.api.model.mag.FieldsOfStudy;
import io.scinapse.api.model.mag.Paper;
import io.scinapse.api.repository.MemberRepository;
import io.scinapse.api.repository.author.AuthorLayerFosRepository;
import io.scinapse.api.repository.author.AuthorLayerPaperHistoryRepository;
import io.scinapse.api.repository.author.AuthorLayerPaperRepository;
import io.scinapse.api.repository.author.AuthorLayerRepository;
import io.scinapse.api.repository.mag.AffiliationRepository;
import io.scinapse.api.repository.mag.FieldsOfStudyRepository;
import io.scinapse.api.repository.mag.PaperAuthorRepository;
import io.scinapse.api.repository.mag.PaperRepository;
import io.scinapse.api.util.IdUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
    private final AuthorLayerFosRepository authorLayerFosRepository;
    private final MemberRepository memberRepository;
    private final PaperAuthorRepository paperAuthorRepository;
    private final PaperRepository paperRepository;
    private final FieldsOfStudyRepository fieldsOfStudyRepository;
    private final AffiliationRepository affiliationRepository;

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

        Optional<AuthorLayer> layerOptional = Optional.ofNullable(authorLayerRepository.findOne(author.getId()));
        if (layerOptional.isPresent()) {
            // the author already has a layer which is probably created by admin users.
            // do not create duplicates.
            // only update author's email.
            layerOptional.get().setEmail(member.getEmail());
            return;
        }

        // the author does not have a layer. Let's create new one.
        createLayer(author, member);
    }

    private void createLayer(Author author, Member member) {
        AuthorLayer authorLayer = new AuthorLayer();

        // from author
        authorLayer.setAuthor(author);
        authorLayer.setAuthorId(author.getId());
        authorLayer.setName(author.getName());
        authorLayer.setLastKnownAffiliation(author.getLastKnownAffiliation());
        authorLayer.setPaperCount(author.getPaperCount());
        authorLayer.setCitationCount(author.getCitationCount());

        // from member
        authorLayer.setEmail(member.getEmail());

        authorLayerRepository.save(authorLayer);

        copyPapers(author, authorLayer);
        initFos(authorLayer);
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

    private void initFos(AuthorLayer layer) {
        List<Long> relatedFosIds = authorLayerFosRepository.getRelatedFos(layer.getAuthorId());
        List<FieldsOfStudy> relatedFos = fieldsOfStudyRepository.findByIdIn(relatedFosIds);
        List<AuthorLayerFos> fosList = relatedFos.stream()
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
        boolean isUpdated = false;

        if (!StringUtils.equals(layer.getBio(), updateDto.getBio())) {
            isUpdated = true;
            layer.setBio(updateDto.getBio());
        }

        if (StringUtils.isNotBlank(updateDto.getName())) {
            isUpdated = true;
            layer.setName(updateDto.getName());
        }

        if (StringUtils.isNotBlank(updateDto.getEmail())) {
            isUpdated = true;
            layer.setEmail(updateDto.getEmail());
        }

        if (!StringUtils.equals(layer.getWebPage(), updateDto.getWebPage())) {
            isUpdated = true;
            layer.setWebPage(updateDto.getWebPage());
        }

        Long updatedAffiliationId = updateDto.getAffiliationId();
        Affiliation lastKnownAffiliation = layer.getLastKnownAffiliation();
        if (updatedAffiliationId == null) {
            if (lastKnownAffiliation != null) { // case : user removes last known affiliation
                isUpdated = true;
                layer.setLastKnownAffiliation(null);
            }
        } else {
            if (updatedAffiliationId <= 0) {
                throw new BadRequestException("Cannot update affiliation with invalid affiliation ID: " + updatedAffiliationId);
            }
            if (lastKnownAffiliation == null || lastKnownAffiliation.getId() != updatedAffiliationId) { // case : user updates last known affiliation
                Affiliation affiliation = Optional.ofNullable(affiliationRepository.findOne(updatedAffiliationId))
                        .orElseThrow(() -> new BadRequestException("Cannot update affiliation with invalid affiliation ID: " + updatedAffiliationId));
                isUpdated = true;
                layer.setLastKnownAffiliation(affiliation);
            }
        }

        if (isUpdated) {
            layer.setStatus(AuthorLayer.LayerStatus.PENDING);
        }

        return layer;
    }

    public List<PaperTitleDto> getAllPaperTitles(AuthorLayer layer) {
        return authorLayerPaperRepository.getAllTitles(layer.getAuthorId())
                .stream()
                .map(obj -> {
                    PaperTitleDto dto = new PaperTitleDto();
                    dto.setPaperId((long) obj[0]);
                    dto.setTitle((String) obj[1]);
                    dto.setSelected((boolean) obj[2]);
                    return dto;
                })
                .collect(Collectors.toList());
    }

}
