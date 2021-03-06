package io.scinapse.api.service.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.controller.PaperController;
import io.scinapse.api.dto.CitationTextDto;
import io.scinapse.api.dto.PaperTitleDto;
import io.scinapse.api.error.ExternalApiCallException;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.util.TextUtils;
import io.scinapse.domain.data.academic.model.*;
import io.scinapse.domain.data.academic.repository.*;
import io.scinapse.domain.data.scinapse.model.PaperRequest;
import io.scinapse.domain.data.scinapse.repository.PaperRequestRepository;
import io.scinapse.domain.enums.CitationFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@XRayEnabled
@Service
@RequiredArgsConstructor
public class PaperService {

    private static final String DOI_PREFIX = "https://doi.org/";

    private final PaperRepository paperRepository;
    private final PaperReferenceRepository paperReferenceRepository;
    private final PaperRecommendationRepository paperRecommendationRepository;
    private final AuthorTopPaperRepository authorTopPaperRepository;
    private final PaperAuthorRepository paperAuthorRepository;
    private final PaperRequestRepository paperRequestRepository;

    private final RestTemplate restTemplate;

    public Paper find(long paperId) {
        return paperRepository.findOne(paperId);
    }

    public List<Paper> findByIdIn(List<Long> paperIds) {
        return paperRepository.findByIdIn(paperIds);
    }

    public List<Long> findReferences(long paperId, PageRequest pageRequest) {
        return paperReferenceRepository.findByPaperId(paperId, pageRequest.toPageable())
                .stream()
                .map(PaperReference::getPaperReferenceId)
                .collect(Collectors.toList());
    }

    public List<Long> findCited(long paperId, PageRequest pageRequest) {
        return paperReferenceRepository.findByPaperReferenceId(paperId, pageRequest.toPageable())
                .stream()
                .map(PaperReference::getPaperId)
                .collect(Collectors.toList());
    }

    public boolean exists(long paperId) {
        return paperRepository.exists(paperId);
    }

    public CitationTextDto citation(String doiStr, CitationFormat format) {
        String doi = TextUtils.parseDoi(doiStr);

        if (!StringUtils.hasText(doi)) {
            throw new ResourceNotFoundException("DOI needed for citation.");
        }

        String doiHttp = DOI_PREFIX + doi;
        URI uri = UriComponentsBuilder.fromHttpUrl(doiHttp).build().toUri();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.valueOf(format.getAccept())));
        HttpEntity entity = new HttpEntity(httpHeaders);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            String formattedCitation = responseEntity.getBody();

            CitationTextDto dto = new CitationTextDto();
            dto.format = format;
            dto.citationText = formattedCitation;

            return dto;
        } catch (HttpClientErrorException e) {
            HttpStatus statusCode = e.getStatusCode();
            if (HttpStatus.NOT_FOUND == statusCode) {
                throw new ResourceNotFoundException("Cannot support citation text.");
            }
            throw new ExternalApiCallException("Request is not successful: " + statusCode + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            // ignore for temp
            return new CitationTextDto();
        }
    }

    public List<Paper> getRelatedPapers(long paperId) {
        return paperRecommendationRepository.findTop5ByPaperIdOrderByScoreDesc(paperId)
                .stream()
                .map(PaperRecommendation::getRecommendedPaper)
                .collect(Collectors.toList());
    }

    public List<Paper> getRecommendedPapers(long paperId) {
        return paperRecommendationRepository.getHighestCitedRecommendationPapers(paperId,
                PageRequest.defaultPageable(3));
    }

    public List<Paper> getAuthorRelatedPapers(long paperId, long authorId) {
        return authorTopPaperRepository.findByAuthorIdAndPaperIdNot(authorId, paperId)
                .stream()
                .map(AuthorTopPaper::getPaper)
                .collect(Collectors.toList());
    }

    public Page<PaperAuthor> getPaperAuthors(long paperId, PageRequest pageRequest) {
        return paperAuthorRepository.getByPaperIdOrderByAuthorSequenceNumber(paperId, pageRequest.toPageable());
    }

    public List<PaperTitleDto> getAllPaperTitle(Set<Long> paperIds) {
        if (CollectionUtils.isEmpty(paperIds)) {
            return new ArrayList<>();
        }
        return paperRepository.findAllPaperTitle(paperIds)
                .stream()
                .map(obj -> {
                    PaperTitleDto dto = new PaperTitleDto();
                    dto.setPaperId((long) obj[0]);
                    dto.setTitle((String) obj[1]);
                    dto.setCitationCount((Long) obj[2]);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void requestPaper(long paperId, PaperController.PaperRequestWrapper request, Long memberId) {
        PaperRequest paperRequest = new PaperRequest();
        paperRequest.setPaperId(paperId);
        paperRequest.setEmail(request.getEmail());
        paperRequest.setName(request.getName());
        paperRequest.setMessage(request.getMessage());
        paperRequest.setMemberId(memberId);
        paperRequestRepository.save(paperRequest);
    }
}
