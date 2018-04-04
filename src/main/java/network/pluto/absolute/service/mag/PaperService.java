package network.pluto.absolute.service.mag;

import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.CitationTextDto;
import network.pluto.absolute.enums.CitationFormat;
import network.pluto.absolute.error.ExternalApiCallException;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.util.TextUtils;
import network.pluto.bibliotheca.models.mag.Paper;
import network.pluto.bibliotheca.models.mag.PaperAbstract;
import network.pluto.bibliotheca.models.mag.RelPaperReference;
import network.pluto.bibliotheca.repositories.mag.PaperAbstractRepository;
import network.pluto.bibliotheca.repositories.mag.PaperRepository;
import network.pluto.bibliotheca.repositories.mag.RelPaperReferenceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PaperService {

    private static final String DOI_PREFIX = "https://doi.org/";

    private final PaperRepository paperRepository;
    private final PaperAbstractRepository paperAbstractRepository;
    private final RelPaperReferenceRepository paperReferenceRepository;
    private final RestTemplate restTemplate;

    public Paper find(long paperId) {
        Paper paper = paperRepository.findOne(paperId);
        if (paper == null) {
            return null;
        }

        PaperAbstract paperAbstract = paperAbstractRepository.findOne(paperId);
        paper.setPaperAbstract(paperAbstract);
        return paper;
    }

    public List<Paper> findByIdIn(List<Long> paperIds) {
        List<Paper> papers = paperRepository.findByIdIn(paperIds);

        Map<Long, PaperAbstract> abstractMap = paperAbstractRepository.findByPaperIdIn(paperIds)
                .stream()
                .collect(Collectors.toMap(
                        PaperAbstract::getPaperId,
                        Function.identity()
                ));
        papers.forEach(p -> p.setPaperAbstract(abstractMap.get(p.getId())));

        return papers;
    }

    public Page<Long> findReferences(long paperId, Pageable pageable) {
        return paperReferenceRepository.findByPaperId(paperId, pageable).map(RelPaperReference::getPaperReferenceId);
    }

    public Page<Long> findCited(long paperId, Pageable pageable) {
        return paperReferenceRepository.findByPaperReferenceId(paperId, pageable).map(RelPaperReference::getPaperId);
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
        }
    }

}
