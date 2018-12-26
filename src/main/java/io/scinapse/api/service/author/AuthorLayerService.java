package io.scinapse.api.service.author;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.configuration.ScinapseConstant;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.data.academic.Paper;
import io.scinapse.api.data.academic.repository.*;
import io.scinapse.api.data.scinapse.model.Member;
import io.scinapse.api.data.scinapse.model.author.*;
import io.scinapse.api.data.scinapse.repository.MemberRepository;
import io.scinapse.api.data.scinapse.repository.author.*;
import io.scinapse.api.dto.PaperTitleDto;
import io.scinapse.api.dto.mag.*;
import io.scinapse.api.enums.PaperSort;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.service.mag.PaperService;
import io.scinapse.api.util.IdUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final AuthorLayerCoauthorRepository authorLayerCoauthorRepository;
    private final AuthorRepository authorRepository;
    private final MemberRepository memberRepository;
    private final PaperAuthorRepository paperAuthorRepository;
    private final PaperRepository paperRepository;
    private final PaperService paperService;
    private final FieldsOfStudyRepository fieldsOfStudyRepository;
    private final AffiliationRepository affiliationRepository;

    private final AuthorEducationRepository educationRepository;
    private final AuthorExperienceRepository experienceRepository;
    private final AuthorAwardRepository awardRepository;

    private final Environment environment;

    @Value("${pluto.server.slack.author.url}")
    private String slackUrl;

    @Value("${pluto.server.web.url}")
    private String webPageUrl;

    private final AsyncRestTemplate restTemplate = new AsyncRestTemplate();

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
        AuthorLayer savedLayer = createLayer(author, dto);

        if (environment.acceptsProfiles("prod")) {
            sendSlackAlarm(member, author, savedLayer);
        }
    }

    @Transactional
    public void disconnect(long authorId) {
        Optional.ofNullable(memberRepository.findByAuthorId(authorId)).ifPresent(member -> member.setAuthorId(null));
        authorLayerCoauthorRepository.deleteByIdAuthorId(authorId);
        authorLayerFosRepository.deleteByIdAuthorId(authorId);
        authorLayerPaperHistoryRepository.deleteByAuthorId(authorId);
        authorLayerPaperRepository.deleteByIdAuthorId(authorId);

        if (authorLayerRepository.exists(authorId)) {
            authorLayerRepository.delete(authorId);
        }
    }

    private AuthorLayer createLayer(AuthorDto author, AuthorLayerUpdateDto dto) {
        AuthorLayer authorLayer = new AuthorLayer();

        // from author
        authorLayer.setAuthorId(author.getId());
        authorLayer.setName(author.getName());
        authorLayer.setPaperCount(author.getPaperCount());
        authorLayer.setCitationCount(author.getCitationCount());

        Optional.ofNullable(author.getLastKnownAffiliation())
                .ifPresent(aff -> authorLayer.setLastKnownAffiliationId(aff.getId()));

        AuthorLayer saved = authorLayerRepository.saveAndFlush(authorLayer);

        List<AuthorLayerPaper> savedPapers = copyPapers(author, saved);

        Set<Long> paperIds = savedPapers
                .stream()
                .map(AuthorLayerPaper::getId)
                .map(AuthorLayerPaper.AuthorLayerPaperId::getPaperId)
                .collect(Collectors.toSet());

        updateByPapers(saved, paperIds);
        update(saved, dto);

        return saved;
    }

    private void sendSlackAlarm(Member member, AuthorDto original, AuthorLayer connected) {
        Map<String, Object> slackData = new HashMap<>();
        slackData.put("text", "Author connection occurs!! "
                + "member: [ " + member.getId()
                + " ], member name: [ " + StringUtils.join(new String[] { member.getFirstName(), member.getLastName() }, " ")
                + " ], member email: [ " + member.getEmail()
                + " ], connected author: [ " + webPageUrl + "/authors/" + original.getId()
                + " ], original name: [ " + original.getName()
                + " ], connected name: [ " + connected.getName()
                + " ], connected email: [ " + connected.getEmail()
                + " ]");
        HttpEntity<Map<String, Object>> body = new HttpEntity<>(slackData);
        restTemplate.postForEntity(slackUrl, body, String.class);
    }

    private List<AuthorLayerPaper> copyPapers(AuthorDto author, AuthorLayer layer) {
        List<AuthorLayerPaper> layerPapers = paperAuthorRepository.findByIdAuthorId(author.getId())
                .stream()
                .map(paperAuthor -> {
                    AuthorLayerPaper layerPaper = new AuthorLayerPaper(layer.getAuthorId(), paperAuthor.getId().getPaperId());

                    Optional.ofNullable(paperAuthor.getAffiliation())
                            .ifPresent(aff -> layerPaper.setAffiliationId(aff.getId()));
                    layerPaper.setAuthorSequenceNumber(paperAuthor.getAuthorSequenceNumber());

                    // for sorting & filtering
                    layerPaper.setTitle(paperAuthor.getPaper().getTitle());
                    layerPaper.setYear(paperAuthor.getPaper().getYear());
                    layerPaper.setCitationCount(paperAuthor.getPaper().getCitationCount());

                    return layerPaper;
                })
                .collect(Collectors.toList());

        layerPapers.stream()
                .sorted(Comparator.comparing(AuthorLayerPaper::getCitationCount).reversed())
                .limit(3)
                .forEach(lp -> lp.setRepresentative(true));

        return authorLayerPaperRepository.save(layerPapers);
    }

    private void updateByPapers(AuthorLayer layer) {
        Set<Long> paperIds = authorLayerPaperRepository.findAllLayerPapers(layer.getAuthorId())
                .stream()
                .map(AuthorLayerPaper::getId)
                .map(AuthorLayerPaper.AuthorLayerPaperId::getPaperId)
                .collect(Collectors.toSet());
        updateByPapers(layer, paperIds);
    }

    private void updateByPapers(AuthorLayer layer, Set<Long> paperIds) {
        updateMetric(layer, paperIds);
        updateFos(layer, paperIds);
        updateCoauthor(layer, paperIds);
    }

    private void updateMetric(AuthorLayer layer, Set<Long> paperIds) {
        List<PaperTitleDto> allPaperTitle = paperService.getAllPaperTitle(paperIds);

        List<Long> citationCounts = allPaperTitle
                .stream()
                .map(PaperTitleDto::getCitationCount)
                .filter(Objects::nonNull)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        int paperCount = allPaperTitle.size();
        long citationCount = citationCounts.stream().mapToLong(Long::longValue).sum();
        int hindex = 0;
        for (Long count : citationCounts) {
            if (count > hindex) {
                hindex++;
                continue;
            }
            break;
        }

        layer.setPaperCount(paperCount);
        layer.setCitationCount(citationCount);
        layer.setHindex(hindex);
    }

    private void updateFos(AuthorLayer layer, Set<Long> paperIds) {
        List<Long> relatedFosIds = paperRepository.calculateFos(paperIds);

        AtomicInteger rankCounter = new AtomicInteger(1);
        List<AuthorLayerFos> fosList = relatedFosIds
                .stream()
                .map(fosId -> new AuthorLayerFos(layer, fosId, rankCounter.getAndIncrement()))
                .collect(Collectors.toList());

        authorLayerFosRepository.deleteByIdAuthorId(layer.getAuthorId());
        List<AuthorLayerFos> saved = authorLayerFosRepository.save(fosList);
        layer.setFosList(saved);
    }

    private void updateCoauthor(AuthorLayer layer, Set<Long> paperIds) {
        List<Long> relatedAuthorIds = paperRepository.calculateCoauthor(layer.getAuthorId(), paperIds);

        AtomicInteger rankCounter = new AtomicInteger(1);
        List<AuthorLayerCoauthor> coauthors = relatedAuthorIds
                .stream()
                .map(authorId -> new AuthorLayerCoauthor(layer, authorId, rankCounter.getAndIncrement()))
                .collect(Collectors.toList());

        authorLayerCoauthorRepository.deleteByIdAuthorId(layer.getAuthorId());
        List<AuthorLayerCoauthor> saved = authorLayerCoauthorRepository.save(coauthors);
        layer.setCoauthors(saved);
    }

    public boolean exists(long authorId) {
        return authorLayerRepository.exists(authorId);
    }

    public Optional<AuthorLayer> find(long authorId) {
        return Optional.ofNullable(authorLayerRepository.findOne(authorId));
    }

    public List<AuthorDto> findCoauthors(AuthorLayer layer) {
        if (CollectionUtils.isEmpty(layer.getCoauthors())) {
            return new ArrayList<>();
        }

        List<Long> coauthorIds = layer.getCoauthors()
                .stream()
                .sorted(Comparator.comparing(AuthorLayerCoauthor::getRank))
                .map(AuthorLayerCoauthor::getId)
                .map(AuthorLayerCoauthor.AuthorLayerCoauthorId::getCoauthorId)
                .collect(Collectors.toList());

        Map<Long, AuthorDto> authorMap = authorRepository.findByIdIn(coauthorIds)
                .stream()
                .map(AuthorDto::new)
                .collect(Collectors.toMap(
                        AuthorDto::getId,
                        Function.identity()
                ));

        return coauthorIds
                .stream()
                .map(authorMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Page<AuthorLayerPaper> findPapers(AuthorLayer layer, String[] keywords, PageRequest pageRequest) {
        PaperSort sort = PaperSort.find(pageRequest.getSort());
        return authorLayerPaperRepository.findPapers(layer.getAuthorId(), false, keywords, sort, pageRequest.toPageable());
    }

    @Transactional
    public void removePapers(AuthorLayer layer, Set<Long> paperIds) {
        List<AuthorLayerPaper> layerPapers = authorLayerPaperRepository.findByIdAuthorIdAndIdPaperIdIn(layer.getAuthorId(), paperIds);
        if (CollectionUtils.isEmpty(layerPapers)) {
            return;
        }

        layerPapers
                .forEach((paper) -> {
                    // set layer paper's status
                    paper.setStatus(AuthorLayerPaper.PaperStatus.PENDING_REMOVE);

                    // remove from representative publication
                    paper.setRepresentative(false);
                });

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

        updateByPapers(layer);
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
        List<Paper> existingPapers = paperRepository.findByIdIn(new ArrayList<>(paperIds));
        List<AuthorLayerPaper> layerPapers = existingPapers
                .stream()
                .map(paper -> {
                    AuthorLayerPaper layerPaper = new AuthorLayerPaper(layer.getAuthorId(), paper.getId());
                    layerPaper.setStatus(AuthorLayerPaper.PaperStatus.PENDING_ADD);

                    // for sorting & filtering
                    layerPaper.setTitle(paper.getTitle());
                    layerPaper.setYear(paper.getYear());
                    layerPaper.setCitationCount(paper.getCitationCount());

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

        updateByPapers(layer);
    }

    @Transactional
    public List<AuthorLayerPaper> updateRepresentative(AuthorLayer layer, Set<Long> representativePaperIds) {
        if (representativePaperIds.size() > 5) {
            throw new BadRequestException("Author can select up to 5 papers as representative. Selected size: " + representativePaperIds.size());
        }

        List<AuthorLayerPaper> layerPapersToRepresent = authorLayerPaperRepository.findByIdAuthorIdAndIdPaperIdIn(layer.getAuthorId(), representativePaperIds);

        List<Long> removedPaperIds = layerPapersToRepresent
                .stream()
                .filter(lp -> lp.getStatus() == AuthorLayerPaper.PaperStatus.PENDING_REMOVE)
                .map(lp -> lp.getId().getPaperId())
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(removedPaperIds)) {
            throw new BadRequestException("Cannot include papers to be removed as a representative publication: " + removedPaperIds);
        }

        // unmark existing representative publications
        authorLayerPaperRepository.findRepresentativePapers(layer.getAuthorId())
                .forEach(lp -> lp.setRepresentative(false));

        // mark newly representative publications
        layerPapersToRepresent
                .forEach(lp -> lp.setRepresentative(true));

        return layerPapersToRepresent;
    }

    public List<AuthorLayerPaper> findRepresentativePapers(long authorId) {
        return authorLayerPaperRepository.findRepresentativePapers(authorId);
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

    @Transactional
    public String updateProfileImage(AuthorLayer layer, Member member, String profileImageKey) {
        layer.setProfileImage(profileImageKey);
        member.setProfileImage(profileImageKey);
        return ScinapseConstant.SCINAPSE_MEDIA_URL + profileImageKey;
    }

    @Transactional
    public void deleteProfileImage(AuthorLayer layer, Member member) {
        layer.setProfileImage(null);
        member.setProfileImage(null);
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
                    dto.setHIndex(layer.getHindex());
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
                    dto.setHIndex(layer.getHindex());
                    Optional.ofNullable(layer.getProfileImage())
                            .ifPresent(key -> dto.setProfileImageUrl(ScinapseConstant.SCINAPSE_MEDIA_URL + key));
                });

        return dtos;
    }

    public void decorateAuthorDetail(AuthorDto dto, AuthorLayer layer) {
        dto.setLayered(true);
        dto.setName(layer.getName());
        dto.setEmail(layer.getEmail());
        dto.setPaperCount(layer.getPaperCount());
        dto.setCitationCount(layer.getCitationCount());
        dto.setHIndex(layer.getHindex());
        dto.setBio(layer.getBio());
        dto.setWebPage(layer.getWebPage());
        Optional.ofNullable(layer.getProfileImage())
                .ifPresent(key -> dto.setProfileImageUrl(ScinapseConstant.SCINAPSE_MEDIA_URL + key));

        Optional.ofNullable(layer.getLastKnownAffiliationId())
                .map(affiliationRepository::findOne)
                .map(AffiliationDto::new)
                .ifPresent(dto::setLastKnownAffiliation);

        if (!CollectionUtils.isEmpty(layer.getFosList())) {
            List<Long> fosIds = layer.getFosList()
                    .stream()
                    .sorted(Comparator.comparing(AuthorLayerFos::getRank))
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

    @Transactional
    public AuthorEducation addEducation(AuthorLayer layer, AuthorEducation education) {
        checkDateValidity(education.getStartDate(), education.getEndDate());

        education.setId(IdUtils.generateStringId(educationRepository));
        education.setAuthor(layer);
        return educationRepository.save(education);
    }

    public Optional<AuthorEducation> findEducation(String educationId) {
        return Optional.ofNullable(educationRepository.findOne(educationId));
    }

    @Transactional
    public AuthorEducation updateEducation(AuthorEducation old, AuthorEducation updated) {
        checkDateValidity(updated.getStartDate(), updated.getEndDate());

        old.setStartDate(updated.getStartDate());
        old.setEndDate(updated.getEndDate());
        old.setCurrent(updated.isCurrent());
        old.setInstitution(updated.getInstitution());
        old.setDepartment(updated.getDepartment());
        old.setDegree(updated.getDegree());
        return old;
    }

    @Transactional
    public void deleteEducation(AuthorEducation education) {
        educationRepository.delete(education);
    }

    @Transactional
    public AuthorExperience addExperience(AuthorLayer layer, AuthorExperience experience) {
        checkDateValidity(experience.getStartDate(), experience.getEndDate());

        experience.setId(IdUtils.generateStringId(experienceRepository));
        experience.setAuthor(layer);
        return experienceRepository.save(experience);
    }

    public Optional<AuthorExperience> findExperience(String experienceId) {
        return Optional.ofNullable(experienceRepository.findOne(experienceId));
    }

    @Transactional
    public AuthorExperience updateExperience(AuthorExperience old, AuthorExperience updated) {
        checkDateValidity(updated.getStartDate(), updated.getEndDate());

        old.setStartDate(updated.getStartDate());
        old.setEndDate(updated.getEndDate());
        old.setCurrent(updated.isCurrent());
        old.setInstitution(updated.getInstitution());
        old.setDepartment(updated.getDepartment());
        old.setPosition(updated.getPosition());
        old.setDescription(updated.getDescription());
        return old;
    }

    @Transactional
    public void deleteExperience(AuthorExperience experience) {
        experienceRepository.delete(experience);
    }

    @Transactional
    public AuthorAward addAward(AuthorLayer layer, AuthorAward award) {
        award.setId(IdUtils.generateStringId(awardRepository));
        award.setAuthor(layer);
        return awardRepository.save(award);
    }

    public Optional<AuthorAward> findAward(String awardId) {
        return Optional.ofNullable(awardRepository.findOne(awardId));
    }

    @Transactional
    public AuthorAward updateAward(AuthorAward old, AuthorAward updated) {
        old.setReceivedDate(updated.getReceivedDate());
        old.setTitle(updated.getTitle());
        old.setDescription(updated.getDescription());
        return old;
    }

    @Transactional
    public void deleteAward(AuthorAward award) {
        awardRepository.delete(award);
    }

    private void checkDateValidity(Date startDate, Date endDate) {
        if (startDate == null) {
            throw new BadRequestException("start date is required.");
        }
        if (endDate == null) {
            return;
        }
        if (startDate.after(endDate)) {
            throw new BadRequestException("start date must be set before end date.");
        }
    }

}
