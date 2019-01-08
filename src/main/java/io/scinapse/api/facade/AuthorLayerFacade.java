package io.scinapse.api.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.data.scinapse.model.Member;
import io.scinapse.api.data.scinapse.model.author.*;
import io.scinapse.api.dto.PaperTitleDto;
import io.scinapse.api.dto.author.AuthorAwardDto;
import io.scinapse.api.dto.author.AuthorEducationDto;
import io.scinapse.api.dto.author.AuthorExperienceDto;
import io.scinapse.api.dto.author.AuthorInfoDto;
import io.scinapse.api.dto.mag.AuthorDto;
import io.scinapse.api.dto.mag.AuthorLayerUpdateDto;
import io.scinapse.api.dto.mag.AuthorPaperDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.error.ResourceNotFoundException;
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
        return findDetailed(authorId, true);
    }

    @Transactional
    public void disconnect(long authorId) {
        layerService.disconnect(authorId);
    }

    public AuthorDto findDetailed(long authorId, boolean includeEmail) {
        AuthorDto dto = authorFacade.find(authorId);
        Optional<AuthorLayer> layer = layerService.find(authorId);

        // just return original author if it does not have a layer.
        if (!layer.isPresent()) {
            return dto;
        }

        // put detailed information.
        layerService.decorateAuthorDetail(dto, layer.get(), includeEmail);

        // add representative publication information.
        List<Long> paperIds = layerService.findRepresentativePapers(authorId)
                .stream()
                .map(AuthorLayerPaper::getId)
                .map(AuthorLayerPaper.AuthorLayerPaperId::getPaperId)
                .collect(Collectors.toList());
        List<PaperDto> representative = paperFacade.findIn(paperIds, PaperConverter.detail());
        dto.setRepresentativePapers(representative);

        return dto;
    }

    @Transactional
    public void removePapers(Member member, boolean admin, long authorId, Set<Long> paperIds) {
        AuthorLayer layer = findLayer(authorId);
        if (!admin) {
            checkOwner(member, layer.getAuthorId());
        }

        layerService.removePapers(layer, paperIds);
    }

    @Transactional
    public void addPapers(Member member, boolean admin, long authorId, Set<Long> paperIds) {
        AuthorLayer layer = findLayer(authorId);
        if (!admin) {
            checkOwner(member, layer.getAuthorId());
        }

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
        return findDetailed(authorId, true);
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

    public AuthorInfoDto getInformation(long authorId) {
        AuthorLayer layer = findLayer(authorId);
        return new AuthorInfoDto(layer);
    }

    @Transactional
    public AuthorEducationDto addEducation(Member member, long authorId, AuthorEducationDto dto) {
        AuthorLayer layer = findLayer(authorId);
        checkOwner(member, layer.getAuthorId());

        AuthorEducation education = layerService.addEducation(layer, dto.toEntity());
        return new AuthorEducationDto(education);
    }

    @Transactional
    public AuthorEducationDto updateEducation(Member member, String educationId, AuthorEducationDto updatedDto) {
        AuthorEducation education = layerService.findEducation(educationId)
                .orElseThrow(() -> new BadRequestException("Author education[" + educationId + "] dose not exist."));

        checkOwner(member, education.getAuthor().getAuthorId());

        AuthorEducation updated = layerService.updateEducation(education, updatedDto.toEntity());
        return new AuthorEducationDto(updated);
    }

    @Transactional
    public void deleteEducation(Member member, String educationId) {
        AuthorEducation education = layerService.findEducation(educationId)
                .orElseThrow(() -> new BadRequestException("Author education[" + educationId + "] dose not exist."));

        checkOwner(member, education.getAuthor().getAuthorId());

        layerService.deleteEducation(education);
    }

    @Transactional
    public AuthorExperienceDto addExperience(Member member, long authorId, AuthorExperienceDto dto) {
        AuthorLayer layer = findLayer(authorId);
        checkOwner(member, layer.getAuthorId());

        AuthorExperience experience = layerService.addExperience(layer, dto.toEntity());
        return new AuthorExperienceDto(experience);
    }

    @Transactional
    public AuthorExperienceDto updateExperience(Member member, String experienceId, AuthorExperienceDto updatedDto) {
        AuthorExperience experience = layerService.findExperience(experienceId)
                .orElseThrow(() -> new BadRequestException("Author experience[" + experienceId + "] dose not exist."));

        checkOwner(member, experience.getAuthor().getAuthorId());

        AuthorExperience updated = layerService.updateExperience(experience, updatedDto.toEntity());
        return new AuthorExperienceDto(updated);
    }

    @Transactional
    public void deleteExperience(Member member, String experienceId) {
        AuthorExperience experience = layerService.findExperience(experienceId)
                .orElseThrow(() -> new BadRequestException("Author experience[" + experienceId + "] dose not exist."));

        checkOwner(member, experience.getAuthor().getAuthorId());

        layerService.deleteExperience(experience);
    }

    @Transactional
    public AuthorAwardDto addAward(Member member, long authorId, AuthorAwardDto dto) {
        AuthorLayer layer = findLayer(authorId);
        checkOwner(member, layer.getAuthorId());

        AuthorAward award = layerService.addAward(layer, dto.toEntity());
        return new AuthorAwardDto(award);
    }

    @Transactional
    public AuthorAwardDto updateAward(Member member, String awardId, AuthorAwardDto updatedDto) {
        AuthorAward award = layerService.findAward(awardId)
                .orElseThrow(() -> new ResourceNotFoundException("Author award[" + awardId + "] dose not exist."));

        checkOwner(member, award.getAuthor().getAuthorId());

        AuthorAward updated = layerService.updateAward(award, updatedDto.toEntity());
        return new AuthorAwardDto(updated);
    }

    @Transactional
    public void deleteAward(Member member, String awardId) {
        AuthorAward award = layerService.findAward(awardId)
                .orElseThrow(() -> new ResourceNotFoundException("Author award[" + awardId + "] dose not exist."));

        checkOwner(member, award.getAuthor().getAuthorId());

        layerService.deleteAward(award);
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
