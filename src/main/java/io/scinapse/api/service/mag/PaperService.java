package io.scinapse.api.service.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.dto.CitationTextDto;
import io.scinapse.api.enums.CitationFormat;
import io.scinapse.api.error.ExternalApiCallException;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.model.mag.Paper;
import io.scinapse.api.model.mag.PaperRecommendation;
import io.scinapse.api.model.mag.RelPaperReference;
import io.scinapse.api.repository.mag.PaperAuthorAffiliationRepository;
import io.scinapse.api.repository.mag.PaperRecommendationRepository;
import io.scinapse.api.repository.mag.PaperRepository;
import io.scinapse.api.repository.mag.RelPaperReferenceRepository;
import io.scinapse.api.util.TextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PaperService {

    private static final String DOI_PREFIX = "https://doi.org/";

    private final PaperRepository paperRepository;
    private final RelPaperReferenceRepository paperReferenceRepository;
    private final PaperAuthorAffiliationRepository paperAuthorAffiliationRepository;
    private final PaperRecommendationRepository paperRecommendationRepository;
    private RestTemplate restTemplateForCitation;

    @PostConstruct
    public void setup() {
        restTemplateForCitation = new RestTemplateBuilder().setConnectTimeout(3000).setReadTimeout(2000).build();
        restTemplateForCitation.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
    }

    public Paper find(long paperId) {
        return paperRepository.findOne(paperId);
    }

    public List<Paper> findByIdIn(List<Long> paperIds) {
        return paperRepository.findByIdIn(paperIds);
    }

    public List<Long> findReferences(long paperId, Pageable pageable) {
        return paperReferenceRepository.findByPaperId(paperId, pageable)
                .stream()
                .map(RelPaperReference::getPaperReferenceId)
                .collect(Collectors.toList());
    }

    public List<Long> findCited(long paperId, Pageable pageable) {
        return paperReferenceRepository.findByPaperReferenceId(paperId, pageable)
                .stream()
                .map(RelPaperReference::getPaperId)
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
            ResponseEntity<String> responseEntity = restTemplateForCitation.exchange(uri, HttpMethod.GET, entity, String.class);
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

    public List<Long> getRelatedPapers(long paperId) {
        return paperRecommendationRepository.findTop5ByPaperIdOrderByScoreDesc(paperId)
                .stream()
                .map(PaperRecommendation::getRecommendedPaperId)
                .collect(Collectors.toList());
    }

    public List<Paper> getAuthorRelatedPapers(long paperId, long authorId) {
        return paperAuthorAffiliationRepository.getAuthorMainPapers(paperId, authorId, new PageRequest(0, 5));
    }

}
