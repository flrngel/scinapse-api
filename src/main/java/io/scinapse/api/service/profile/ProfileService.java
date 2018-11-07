package io.scinapse.api.service.profile;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.controller.ProfileController;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.model.Member;
import io.scinapse.api.model.mag.Author;
import io.scinapse.api.model.mag.FieldsOfStudy;
import io.scinapse.api.model.profile.*;
import io.scinapse.api.repository.mag.AuthorRepository;
import io.scinapse.api.repository.mag.FieldsOfStudyRepository;
import io.scinapse.api.repository.profile.*;
import io.scinapse.api.util.IdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@XRayEnabled
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileEducationRepository profileEducationRepository;
    private final ProfileExperienceRepository profileExperienceRepository;
    private final ProfileAwardRepository profileAwardRepository;
    private final ProfileSelectedPublicationRepository profileSelectedPublicationRepository;
    private final AuthorRepository authorRepository;
    private final ProfileAuthorRepository profileAuthorRepository;
    private final ProfileFosRepository profileFosRepository;
    private final FieldsOfStudyRepository fieldsOfStudyRepository;

    @Transactional
    public Profile create(Member member, Profile profile, List<Long> authorIds) {
        if (CollectionUtils.isEmpty(authorIds)) {
            throw new BadRequestException("Profile must include at least one author");
        }

        profile.setId(IdUtils.generateStringId(profileRepository));
        Profile saved = profileRepository.save(profile);

//        member.setProfile(saved);
//        saved.setMember(member);

        initAuthors(saved, authorIds);
        initFos(saved, authorIds);

        return saved;
    }

    @Transactional
    public Profile createMyProfile(Member member, List<Long> authorIds) {
        Profile profile = new Profile();
        profile.setFirstName(member.getFirstName());
        profile.setLastName(StringUtils.defaultString(member.getLastName()));
        profile.setAffiliation(member.getAffiliation());
        profile.setEmail(member.getEmail());

        return create(member, profile, authorIds);
    }

    private void initAuthors(Profile profile, List<Long> authorIds) {
        List<Author> candidateAuthors = authorRepository.findByIdIn(authorIds);
        if (CollectionUtils.isEmpty(candidateAuthors)) {
            log.error("There are ghost authors - {}", authorIds);
            throw new BadRequestException("Profile must include at least one author");
        }

        List<Long> candidateAuthorIds = candidateAuthors.stream().map(Author::getId).collect(Collectors.toList());
        List<ProfileAuthor> alreadyConnectedAuthors = profileAuthorRepository.findByIdAuthorIdIn(candidateAuthorIds);
        if (!CollectionUtils.isEmpty(alreadyConnectedAuthors)) {
            log.error("Some authors are already selected by other profiles - {}", alreadyConnectedAuthors);
            throw new BadRequestException("Some authors are already selected by other profiles - " + alreadyConnectedAuthors);
        }

        List<ProfileAuthor> profileAuthors = candidateAuthors.stream()
                .map(author -> new ProfileAuthor(profile, author))
                .collect(Collectors.toList());
        profileAuthorRepository.save(profileAuthors);
    }

    private void initFos(Profile profile, List<Long> authorIds) {
        List<Long> relatedFosIds = profileFosRepository.getRelatedFos(authorIds);
        List<FieldsOfStudy> relatedFos = fieldsOfStudyRepository.findByIdIn(relatedFosIds);
        List<ProfileFos> fosList = relatedFos.stream()
                .map(fos -> new ProfileFos(profile, fos))
                .collect(Collectors.toList());

        List<ProfileFos> saved = profileFosRepository.save(fosList);
        profile.setProfileFosList(saved);
    }

    public Optional<Profile> find(String profileId) {
        return Optional.ofNullable(profileRepository.findOne(profileId));
    }

    public List<ProfileAuthor> getProfileAuthors(String profileId) {
        return profileAuthorRepository.findByIdProfileId(profileId);
    }

    public List<ProfileController.PaperTitleDto> getAllProfilePapers(String profileId) {
        return profileRepository.getAllProfilePapers(profileId);
    }

    public boolean existsProfileAuthor(Profile profile, Author author) {
        return profileAuthorRepository.exists(ProfileAuthor.ProfileAuthorId.of(profile.getId(), author.getId()));
    }

    @Transactional
    public void connectAuthor(Profile profile, Author author) {
        ProfileAuthor profileAuthor = new ProfileAuthor(profile, author);
        profileAuthorRepository.save(profileAuthor);
    }

    @Transactional
    public void disconnectAuthor(Profile profile, Author author) {
        profileAuthorRepository.delete(ProfileAuthor.ProfileAuthorId.of(profile.getId(), author.getId()));
    }

    @Transactional
    public ProfileEducation addEducation(Profile profile, ProfileEducation education) {
        education.setId(IdUtils.generateStringId(profileEducationRepository));
        education.setProfile(profile);
        return profileEducationRepository.save(education);
    }

    public Optional<ProfileEducation> findEducation(String educationId) {
        return Optional.ofNullable(profileEducationRepository.findOne(educationId));
    }

    @Transactional
    public ProfileEducation updateEducation(ProfileEducation old, ProfileEducation updated) {
        old.setStartDate(updated.getStartDate());
        old.setEndDate(updated.getEndDate());
        old.setCurrent(updated.isCurrent());
        old.setInstitution(updated.getInstitution());
        old.setDepartment(updated.getDepartment());
        old.setDegree(updated.getDegree());
        return old;
    }

    @Transactional
    public void deleteEducation(ProfileEducation education) {
        profileEducationRepository.delete(education);
    }

    @Transactional
    public ProfileExperience addExperience(Profile profile, ProfileExperience experience) {
        experience.setId(IdUtils.generateStringId(profileExperienceRepository));
        experience.setProfile(profile);
        return profileExperienceRepository.save(experience);
    }

    public Optional<ProfileExperience> findExperience(String experienceId) {
        return Optional.ofNullable(profileExperienceRepository.findOne(experienceId));
    }

    @Transactional
    public ProfileExperience updateExperience(ProfileExperience old, ProfileExperience updated) {
        old.setStartDate(updated.getStartDate());
        old.setEndDate(updated.getEndDate());
        old.setCurrent(updated.isCurrent());
        old.setInstitution(updated.getInstitution());
        old.setDepartment(updated.getDepartment());
        old.setPosition(updated.getPosition());
        return old;
    }

    @Transactional
    public void deleteExperience(ProfileExperience experience) {
        profileExperienceRepository.delete(experience);
    }

    @Transactional
    public ProfileAward addAward(Profile profile, ProfileAward award) {
        award.setId(IdUtils.generateStringId(profileAwardRepository));
        award.setProfile(profile);
        return profileAwardRepository.save(award);
    }

    public Optional<ProfileAward> findAward(String awardId) {
        return Optional.ofNullable(profileAwardRepository.findOne(awardId));
    }

    @Transactional
    public ProfileAward updateAward(ProfileAward old, ProfileAward updated) {
        old.setReceivedDate(updated.getReceivedDate());
        old.setTitle(updated.getTitle());
        old.setDescription(updated.getDescription());
        return old;
    }

    @Transactional
    public void deleteAward(ProfileAward award) {
        profileAwardRepository.delete(award);
    }

    @Transactional
    public List<ProfileSelectedPublication> updateSelectedPublications(Profile profile, List<ProfileSelectedPublication> publications) {
        profileSelectedPublicationRepository.deleteByProfileId(profile.getId());
        return profileSelectedPublicationRepository.save(publications);
    }

    public List<ProfileSelectedPublication> findSelectedPublications(String profileId) {
        return profileSelectedPublicationRepository.findByIdProfileId(profileId);
    }

}
