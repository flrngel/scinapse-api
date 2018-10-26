package io.scinapse.api.facade;

import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.controller.ProfileController;
import io.scinapse.api.dto.mag.AuthorDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.dto.profile.ProfileAwardDto;
import io.scinapse.api.dto.profile.ProfileDto;
import io.scinapse.api.dto.profile.ProfileEducationDto;
import io.scinapse.api.dto.profile.ProfileExperienceDto;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.model.Member;
import io.scinapse.api.model.mag.Author;
import io.scinapse.api.model.mag.Paper;
import io.scinapse.api.model.profile.*;
import io.scinapse.api.service.mag.AuthorService;
import io.scinapse.api.service.mag.PaperService;
import io.scinapse.api.service.profile.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Component
@RequiredArgsConstructor
public class ProfileFacade {

    private final ProfileService profileService;
    private final PaperService paperService;
    private final PaperFacade paperFacade;
    private final AuthorService authorService;

    @Transactional
    public ProfileDto createProfile(Member member, ProfileDto dto) {
        Optional.ofNullable(member.getProfile())
                .ifPresent((profile) -> new BadRequestException("User already has a profile: " + profile.getId()));

        Profile created = profileService.create(member, dto.toEntity(), dto.getAuthorIds());
        return convert(created, member.getId());
    }

    @Transactional
    public ProfileDto createMyProfile(Member member, List<Long> authorIds) {
        Optional.ofNullable(member.getProfile())
                .ifPresent((profile) -> new BadRequestException("User already has a profile: " + profile.getId()));

        Profile created = profileService.createMyProfile(member, authorIds);
        return convert(created, member.getId());
    }

    public ProfileDto findProfile(Member member) {
        Profile profile = find(member);
        return convert(profile, member.getId());
    }

    public ProfileDto findProfile(String profileId, Long memberId) {
        Profile profile = find(profileId);
        return convert(profile, memberId);
    }

    private ProfileDto convert(Profile profile, Long memberId) {
        List<Paper> papers = profileService.findSelectedPublications(profile.getId()).stream()
                .map(ProfileSelectedPublication::getPaper)
                .collect(Collectors.toList());
        List<PaperDto> paperDtos = paperFacade.convert(papers, PaperDto.compact());

        ProfileDto dto = new ProfileDto(profile);
        dto.setSelectedPublications(paperDtos);
        dto.setMine(memberId);
        return dto;
    }

    private Profile find(String profileId) {
        return profileService.find(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileId));
    }

    private Profile find(Member member) {
        return Optional.ofNullable(member.getProfile())
                .orElseThrow(() -> new ResourceNotFoundException("Member does not have a profile. Member ID: " + member.getId()));
    }

    public List<AuthorDto> getConnectedAuthors(String profileId) {
        return profileService.getProfileAuthors(profileId)
                .stream()
                .map(ProfileAuthor::getAuthor)
                .map(AuthorDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public AuthorDto connectAuthor(Member member, String profileId, long authorId) {
        Profile profile = find(profileId);
        checkProfileOwner(member, profile);

        Author author = authorService.find(authorId)
                .orElseThrow(() -> new BadRequestException("The author[" + authorId + "] is not exist. Cannot connect with profile."));

        if (profileService.existsProfileAuthor(profile, author)) {
            throw new BadRequestException("The author[" + authorId + "] is already connected to other profile[" + profileId + "]");
        }

        profileService.connectAuthor(profile, author);
        return new AuthorDto(author);
    }

    @Transactional
    public void disconnectAuthor(Member member, String profileId, long authorId) {
        Profile profile = find(profileId);
        checkProfileOwner(member, profile);

        Author connectedAuthor = authorService.find(authorId)
                .orElseThrow(() -> new BadRequestException("The author not found: " + authorId));

        if (!profileService.existsProfileAuthor(profile, connectedAuthor)) {
            throw new BadRequestException("The author[" + authorId + "] is not connected to the profile[" + profileId + "]");
        }

        profileService.disconnectAuthor(profile, connectedAuthor);
    }

    public Page<PaperDto> getProfilePapers(String profileId, PageRequest pageRequest) {
        List<Author> authors = profileService.getProfileAuthors(profileId).stream().map(ProfileAuthor::getAuthor).collect(Collectors.toList());
        List<Long> authorIds = authors.stream().map(Author::getId).collect(Collectors.toList());

        Page<Paper> papers = authorService.getAuthorPaper(authorIds, pageRequest);
        List<PaperDto> paperDtos = paperFacade.convert(papers.getContent(), PaperDto.detail());
        return pageRequest.toPage(paperDtos, papers.getTotalElements());
    }

    public List<ProfileController.PaperTitleDto> getAllProfilePapers(String profileId) {
        return profileService.getAllProfilePapers(profileId);
    }

    @Transactional
    public ProfileEducationDto addEducation(Member member, String profileId, ProfileEducationDto dto) {
        Profile profile = find(profileId);
        checkProfileOwner(member, profile);

        ProfileEducation education = profileService.addEducation(profile, dto.toEntity());
        return new ProfileEducationDto(education);
    }

    @Transactional
    public ProfileEducationDto updateEducation(Member member, String educationId, ProfileEducationDto updatedDto) {
        ProfileEducation education = profileService.findEducation(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile education not found: " + educationId));

        checkProfileOwner(member, education.getProfile());
        ProfileEducation updated = profileService.updateEducation(education, updatedDto.toEntity());
        return new ProfileEducationDto(updated);
    }

    @Transactional
    public void deleteEducation(Member member, String educationId) {
        ProfileEducation education = profileService.findEducation(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile education not found: " + educationId));

        checkProfileOwner(member, education.getProfile());
        profileService.deleteEducation(education);
    }

    @Transactional
    public ProfileExperienceDto addExperience(Member member, String profileId, ProfileExperienceDto dto) {
        Profile profile = find(profileId);
        checkProfileOwner(member, profile);

        ProfileExperience experience = profileService.addExperience(profile, dto.toEntity());
        return new ProfileExperienceDto(experience);
    }

    @Transactional
    public ProfileExperienceDto updateExperience(Member member, String experienceId, ProfileExperienceDto updatedDto) {
        ProfileExperience experience = profileService.findExperience(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile experience not found: " + experienceId));

        checkProfileOwner(member, experience.getProfile());
        ProfileExperience updated = profileService.updateExperience(experience, updatedDto.toEntity());
        return new ProfileExperienceDto(updated);
    }

    @Transactional
    public void deleteExperience(Member member, String experienceId) {
        ProfileExperience experience = profileService.findExperience(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile experience not found: " + experienceId));

        checkProfileOwner(member, experience.getProfile());
        profileService.deleteExperience(experience);
    }

    @Transactional
    public ProfileAwardDto addAward(Member member, String profileId, ProfileAwardDto dto) {
        Profile profile = find(profileId);
        checkProfileOwner(member, profile);

        ProfileAward award = profileService.addAward(profile, dto.toEntity());
        return new ProfileAwardDto(award);
    }

    @Transactional
    public ProfileAwardDto updateAward(Member member, String awardId, ProfileAwardDto updatedDto) {
        ProfileAward award = profileService.findAward(awardId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile award not found: " + awardId));

        checkProfileOwner(member, award.getProfile());
        ProfileAward updated = profileService.updateAward(award, updatedDto.toEntity());
        return new ProfileAwardDto(updated);
    }

    @Transactional
    public void deleteAward(Member member, String awardId) {
        ProfileAward award = profileService.findAward(awardId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile award not found: " + awardId));

        checkProfileOwner(member, award.getProfile());
        profileService.deleteAward(award);
    }

    @Transactional
    public List<PaperDto> updateSelectedPublications(Member member, String profileId, List<Long> paperIds) {
        Profile profile = find(profileId);
        checkProfileOwner(member, profile);

        List<Paper> existing = paperService.findByIdIn(paperIds);
        List<ProfileSelectedPublication> newPublications = existing.stream()
                .map(paper -> new ProfileSelectedPublication(profile, paper))
                .collect(Collectors.toList());

        List<ProfileSelectedPublication> saved = profileService.updateSelectedPublications(profile, newPublications);
        List<Paper> savedPublications = saved.stream()
                .map(ProfileSelectedPublication::getPaper)
                .collect(Collectors.toList());

        return paperFacade.convert(savedPublications, PaperDto.compact());
    }

    private void checkProfileOwner(Member member, Profile profile) {
        Member profileMember = Optional.ofNullable(profile.getMember())
                .orElseThrow(() -> new AccessDeniedException("Profile does not have its owner: " + profile.getId()));

        if (profileMember.getId() != member.getId()) {
            throw new AccessDeniedException("Only profile's owner can edit the profile. " + "Owner: " + profileMember.getId() + ", Request User: " + member.getId());
        }
    }
}
