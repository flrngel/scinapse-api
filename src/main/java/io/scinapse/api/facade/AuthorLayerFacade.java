package io.scinapse.api.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.data.scinapse.model.Member;
import io.scinapse.api.data.scinapse.model.author.AuthorLayer;
import io.scinapse.api.data.scinapse.model.author.AuthorLayerPaper;
import io.scinapse.api.dto.PaperTitleDto;
import io.scinapse.api.dto.mag.AuthorDto;
import io.scinapse.api.dto.mag.AuthorLayerUpdateDto;
import io.scinapse.api.dto.mag.AuthorPaperDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.service.ImageUploadService;
import io.scinapse.api.service.author.AuthorLayerService;
import io.scinapse.api.service.mag.AuthorService;
import io.scinapse.api.service.mag.PaperConverter;
import io.scinapse.api.service.mag.PaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional(readOnly = true)
@Component
@RequiredArgsConstructor
public class AuthorLayerFacade {

    private final AuthorFacade authorFacade;
    private final PaperFacade paperFacade;
    private final AuthorLayerService layerService;
    private final AuthorService authorService;
    private final PaperService paperService;
    private final ImageUploadService imageUploadService;

    public Page<AuthorPaperDto> findPapers(long authorId, String[] keywords, PageRequest pageRequest) {
        if (!authorService.exists(authorId)) {
            throw new BadRequestException("Author[" + authorId + "] dose not exist.");
        }

        Optional<AuthorLayer> layer = layerService.find(authorId);
        if (layer.isPresent()) {
            Page<AuthorLayerPaper> papers = layerService.findPapers(layer.get(), keywords, pageRequest);

            List<Long> paperIds = papers.getContent()
                    .stream()
                    .map(AuthorLayerPaper::getId)
                    .map(AuthorLayerPaper.AuthorLayerPaperId::getPaperId)
                    .collect(Collectors.toList());

            Map<Long, PaperDto> dtoMap = paperFacade.findMap(paperIds, PaperConverter.detail());

            List<AuthorPaperDto> dtos = papers.getContent()
                    .stream()
                    .map(lp -> {
                        PaperDto paperDto = dtoMap.get(lp.getId().getPaperId());
                        if (paperDto == null) {
                            return null;
                        }
                        return new AuthorPaperDto(paperDto, lp.getStatus(), lp.isRepresentative());
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return new PageImpl<>(dtos, pageRequest.toPageable(), papers.getTotalElements());
        } else {
            return authorFacade.findPapers(authorId, pageRequest);
        }
    }

    public List<AuthorDto> findCoAuthors(long authorId) {
        if (!authorService.exists(authorId)) {
            throw new BadRequestException("Author[" + authorId + "] dose not exist.");
        }

        List<AuthorDto> coAuthors;

        Optional<AuthorLayer> layer = layerService.find(authorId);
        if (layer.isPresent()) {
            coAuthors = layerService.findCoauthors(layer.get());
        } else {
            coAuthors = authorService.findCoAuthors(authorId);
        }

        return layerService.decorateAuthors(coAuthors);
    }

    @Transactional
    public AuthorDto connect(Member member, long authorId, AuthorLayerUpdateDto dto) {
        AuthorDto author = authorFacade.find(authorId);

        layerService.connect(member, author, dto);
        return findDetailed(authorId);
    }

    @Transactional
    public void disconnect(long authorId) {
        layerService.disconnect(authorId);
    }

    public AuthorDto findDetailed(long authorId) {
        AuthorDto dto = authorFacade.find(authorId);
        Optional<AuthorLayer> layer = layerService.find(authorId);

        // just return original author if it does not have a layer.
        if (!layer.isPresent()) {
            return dto;
        }

        // put detailed information.
        layerService.decorateAuthorDetail(dto, layer.get());

        // add representative publication information.
        List<Long> paperIds = layerService.findRepresentativePapers(authorId)
                .stream()
                .map(AuthorLayerPaper::getId)
                .map(AuthorLayerPaper.AuthorLayerPaperId::getPaperId)
                .collect(Collectors.toList());
        List<PaperDto> representative = paperFacade.findIn(paperIds, PaperConverter.detail());
        dto.setRepresentativePapers(representative);
        dto.setSelectedPapers(representative);

        return dto;
    }

    @Transactional
    public void removePapers(Member member, long authorId, Set<Long> paperIds) {
        AuthorLayer layer = findLayer(authorId);
        checkOwner(member, layer.getAuthorId());

        layerService.removePapers(layer, paperIds);
    }

    @Transactional
    public void addPapers(Member member, long authorId, Set<Long> paperIds) {
        AuthorLayer layer = findLayer(authorId);
        checkOwner(member, layer.getAuthorId());

        layerService.addPapers(layer, paperIds);
    }

    @Transactional
    public List<PaperDto> updateRepresentative(Member member, long authorId, Set<Long> representativePaperIds) {
        AuthorLayer layer = findLayer(authorId);
        checkOwner(member, layer.getAuthorId());

        List<Long> paperIds = layerService.updateRepresentative(layer, representativePaperIds)
                .stream()
                .map(AuthorLayerPaper::getId)
                .map(AuthorLayerPaper.AuthorLayerPaperId::getPaperId)
                .collect(Collectors.toList());

        return paperFacade.findIn(paperIds, PaperConverter.detail());
    }

    @Transactional
    public AuthorDto update(Member member, long authorId, AuthorLayerUpdateDto updateDto) {
        AuthorLayer layer = findLayer(authorId);
        checkOwner(member, layer.getAuthorId());

        layerService.update(layer, updateDto);
        return findDetailed(authorId);
    }

    @Transactional
    public String updateProfileImage(Member member, long authorId, MultipartFile profileImageFile) {
        AuthorLayer layer = findLayer(authorId);
        checkOwner(member, layer.getAuthorId());

        String profileImageKey = imageUploadService.uploadImage(ImageUploadService.PROFILE_IMAGE_PATH, profileImageFile);
        return layerService.updateProfileImage(layer, member, profileImageKey);
    }

    @Transactional
    public void deleteProfileImage(Member member, long authorId) {
        AuthorLayer layer = findLayer(authorId);
        checkOwner(member, layer.getAuthorId());

        layerService.deleteProfileImage(layer, member);
    }

    public List<PaperTitleDto> getAllPaperTitles(long authorId) {
        AuthorLayer layer = findLayer(authorId);

        Map<Long, AuthorLayerPaper> layerPaperMap = layerService.getAllLayerPapers(layer.getAuthorId())
                .stream()
                .collect(Collectors.toMap(
                        l -> l.getId().getPaperId(),
                        Function.identity()
                ));

        return paperService.getAllPaperTitle(layerPaperMap.keySet())
                .stream()
                .peek(dto -> Optional.ofNullable(layerPaperMap.get(dto.getPaperId()))
                        .ifPresent(layerPaper -> {
                            dto.setRepresentative(layerPaper.isRepresentative());
                            dto.setSelected(layerPaper.isRepresentative());
                        }))
                .collect(Collectors.toList());
    }

    private AuthorLayer findLayer(long authorId) {
        return layerService.find(authorId)
                .orElseThrow(() -> new BadRequestException("The author[" + authorId + "] does not have a layer."));
    }

    private void checkOwner(Member member, long authorId) {
        if (member.getAuthorId() == null || member.getAuthorId() != authorId) {
            throw new BadRequestException("Member[" + member.getId() + "] is not connected with Author[" + authorId + "].");
        }
    }

}
