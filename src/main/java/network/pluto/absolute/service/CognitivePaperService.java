package network.pluto.absolute.service;

import lombok.extern.slf4j.Slf4j;
import network.pluto.absolute.dto.FosDto;
import network.pluto.absolute.dto.PaperAuthorDto;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.dto.PaperUrlDto;
import network.pluto.absolute.dto.cognitive.CalcHistogramResponseDto;
import network.pluto.absolute.dto.cognitive.EvaluateResponseDto;
import network.pluto.absolute.dto.cognitive.InterpretResponseDto;
import network.pluto.absolute.error.ExternalApiCallException;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.util.Query;
import network.pluto.absolute.util.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CognitivePaperService {

    private static final String DEFAULT_ATTRIBUTES = "Id,Ti,Y,D,AA.DAuN,AA.DAfN,AA.S,E.DN,E.DOI,E.VFN,E.S,CC,RId,F.FN,E";

    private final RestTemplate restTemplate;

    @Value("${pluto.ms.cognitive.uri}")
    private String cognitiveUri;

    private String interpretPath = "/interpret";
    private String evaluatePath = "/evaluate";
    private String calchistogramPath = "/calchistogram";

    @Value("${pluto.ms.cognitive.subscription.key}")
    private String cognitiveSubscriptionKey;

    @Autowired
    public CognitivePaperService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getRecommendQuery(Query query) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(cognitiveUri + interpretPath)
                .queryParam("query", query.getText())
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Ocp-Apim-Subscription-Key", cognitiveSubscriptionKey);

        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<InterpretResponseDto> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, InterpretResponseDto.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new ExternalApiCallException("Response is not successful: " + responseEntity.getStatusCode() + " " + responseEntity.getBody());
        }

        String recommendedQuery = responseEntity.getBody().getRecommendQuery();

        String filter = query.getFilter().toCognitiveFilterQuery();
        if (StringUtils.hasText(recommendedQuery) && StringUtils.hasText(filter)) {
            recommendedQuery = "And(" + recommendedQuery + "," + filter + ")";
        }

        return recommendedQuery;
    }

    public Page<PaperDto> search(String query, Pageable pageable) {
        URI uri = buildUri(evaluatePath);

        LinkedMultiValueMap<String, Object> body = buildRequestBody(query, DEFAULT_ATTRIBUTES + ",E.IA", pageable.getOffset(), pageable.getPageSize());
        HttpEntity<Object> httpEntity = buildHttpEntity(body);

        EvaluateResponseDto response = getResponse(uri, httpEntity);

        List<PaperDto> dtos = response.getEntities().stream()
                .map(detail -> enhance(new PaperDto(), detail))
                .collect(Collectors.toList());

        CalcHistogramResponseDto histogram = getHistogram(query); // for retrieving number of total elements
        return new PageImpl<>(dtos, pageable, histogram.getTotalElements());
    }

    public CalcHistogramResponseDto getHistogram(String query) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(cognitiveUri + calchistogramPath)
                .build()
                .toUri();

        LinkedMultiValueMap<String, Object> body = buildRequestBody(query, null, 0, 0);
        HttpEntity<Object> httpEntity = buildHttpEntity(body);

        ResponseEntity<CalcHistogramResponseDto> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, httpEntity, CalcHistogramResponseDto.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new ExternalApiCallException("Response is not successful: " + responseEntity.getStatusCode() + " " + responseEntity.getBody());
        }

        return responseEntity.getBody();
    }

    public Page<PaperDto> getReferences(long cognitivePaperId, Pageable pageable) {
        EvaluateResponseDto.Entity original = getOriginal(cognitivePaperId);
        long[] pagedReferenceIds = getPagedReferences(original, pageable);

        URI uri = buildUri(evaluatePath);

        String query = buildPaperIdsMatchQuery(pagedReferenceIds);
        LinkedMultiValueMap<String, Object> body = buildRequestBody(query, DEFAULT_ATTRIBUTES + ",E.IA", 0, pageable.getPageSize());
        HttpEntity<Object> httpEntity = buildHttpEntity(body);

        EvaluateResponseDto response = getResponse(uri, httpEntity);

        List<PaperDto> dtos = response.getEntities().stream()
                .map(detail -> enhance(new PaperDto(), detail))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, original.getReferences().length);
    }

    public Page<PaperDto> getCited(long cognitivePaperId, Pageable pageable) {
        URI uri = buildUri(evaluatePath);

        String query = buildCitedMatchQuery(cognitivePaperId);
        LinkedMultiValueMap<String, Object> body = buildRequestBody(query, DEFAULT_ATTRIBUTES + ",E.IA", pageable.getOffset(), pageable.getPageSize());
        HttpEntity<Object> httpEntity = buildHttpEntity(body);

        EvaluateResponseDto response = getResponse(uri, httpEntity);

        List<PaperDto> dtos = response.getEntities().stream()
                .map(detail -> enhance(new PaperDto(), detail))
                .collect(Collectors.toList());

        CalcHistogramResponseDto histogram = getHistogram(query);
        return new PageImpl<>(dtos, pageable, histogram.getTotalElements());
    }

    public List<PaperDto> enhance(List<PaperDto> dtos) {
        if (dtos.isEmpty()) {
            return dtos;
        }

        URI uri = buildUri(evaluatePath);

        String query = buildTitleAndYearMatchQuery(dtos);
        LinkedMultiValueMap<String, Object> body = buildRequestBody(query, DEFAULT_ATTRIBUTES);
        HttpEntity<Object> httpEntity = buildHttpEntity(body);

        EvaluateResponseDto response = getResponse(uri, httpEntity);

        Map<String, EvaluateResponseDto.Entity> entityMap = response.getEntities()
                .stream()
                .collect(Collectors.toMap(
                        EvaluateResponseDto.Entity::getTitleNormalized,
                        Function.identity(),
                        (a, b) -> a // take first element if results have same title key
                ));

        dtos.forEach(dto -> {
            String normalized = TextUtils.normalize(dto.getTitle());

            EvaluateResponseDto.Entity detailFromCognitive = entityMap.get(normalized);
            if (detailFromCognitive == null) {
                return;
            }

            enhance(dto, detailFromCognitive);
        });

        return dtos;
    }

    public PaperDto getCognitivePaper(long cognitivePaperId) {
        EvaluateResponseDto.Entity original = getOriginal(cognitivePaperId);
        return enhance(new PaperDto(), original);
    }

    private EvaluateResponseDto.Entity getOriginal(long cognitivePaperId) {
        URI uri = buildUri(evaluatePath);
        String query = buildPaperIdMatchQuery(cognitivePaperId);
        LinkedMultiValueMap<String, Object> body = buildRequestBody(query, DEFAULT_ATTRIBUTES);
        HttpEntity<Object> httpEntity = buildHttpEntity(body);

        EvaluateResponseDto response = getResponse(uri, httpEntity);

        List<EvaluateResponseDto.Entity> entities = response.getEntities();
        if (entities.size() != 1) {
            throw new ResourceNotFoundException("Cannot retrieve proper paper from cognitivePaperId:" + cognitivePaperId);
        }

        return entities.get(0);
    }

    private URI buildUri(String path) {
        return UriComponentsBuilder
                .fromHttpUrl(cognitiveUri + path)
                .build()
                .toUri();
    }

    private EvaluateResponseDto getResponse(URI uri, HttpEntity<Object> entity) {
        ResponseEntity<EvaluateResponseDto> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, entity, EvaluateResponseDto.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new ExternalApiCallException("Response is not successful: " + responseEntity.getStatusCode() + " " + responseEntity.getBody());
        }
        return responseEntity.getBody();
    }

    private LinkedMultiValueMap<String, Object> buildRequestBody(String query, String attributes) {
        return buildRequestBody(query, attributes, 0, 20);
    }

    private LinkedMultiValueMap<String, Object> buildRequestBody(String query, String attributes, int offset, int count) {
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.put("expr", Collections.singletonList(query));
        body.put("attributes", Collections.singletonList(attributes));
        body.put("offset", Collections.singletonList(Integer.toString(offset)));
        body.put("count", Collections.singletonList(Integer.toString(count)));
        return body;
    }

    private HttpEntity<Object> buildHttpEntity(LinkedMultiValueMap<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Ocp-Apim-Subscription-Key", cognitiveSubscriptionKey); // for api authentication
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return new HttpEntity<>(body, headers);
    }

    private PaperDto enhance(PaperDto dto, EvaluateResponseDto.Entity detailFromCognitive) {
        dto.setCognitivePaperId(detailFromCognitive.getCognitivePaperId());

        if (StringUtils.hasText(detailFromCognitive.getJournalName())) {
            dto.setVenue(detailFromCognitive.getJournalName());
            if (dto.getJournal() != null) {
                dto.getJournal().setFullTitle(detailFromCognitive.getJournalName());
            }
        }

        if (!detailFromCognitive.getAuthors().isEmpty()) {
            List<PaperAuthorDto> authors = detailFromCognitive.getAuthors()
                    .stream()
                    .map(a -> {
                        PaperAuthorDto authorDto = new PaperAuthorDto();
                        authorDto.setName(a.getAuthorDisplayName());
                        authorDto.setOrganization(a.getAffiliationDisplayName());
                        authorDto.setOrder(a.getOrder());
                        authorDto.setPaperId(dto.getId());
                        return authorDto;
                    })
                    .sorted(Comparator.comparingInt(PaperAuthorDto::getOrder))
                    .collect(Collectors.toList());

            dto.setAuthorCount(authors.size());
            dto.setAuthors(authors);
        }

        if (dto.getFosList().isEmpty() && !detailFromCognitive.getFosList().isEmpty()) {
            List<FosDto> fosList = detailFromCognitive.getFosList()
                    .stream()
                    .map(f -> {
                        FosDto fosDto = new FosDto();
                        fosDto.setFos(f.getFosDisplayName());
                        return fosDto;
                    })
                    .collect(Collectors.toList());

            dto.setFosCount(fosList.size());
            dto.setFosList(fosList);
        }

        dto.setReferenceCount(detailFromCognitive.getReferences().length);
        dto.setCitedCount(detailFromCognitive.getCitationCount());

        if (dto.getYear() == 0) {
            dto.setYear(detailFromCognitive.getYear());
        }

        if (!StringUtils.hasText(dto.getTitle())) {
            dto.setTitle(detailFromCognitive.getTitleDisplay());
        }

        if (!StringUtils.hasText(dto.getPaperAbstract()) && detailFromCognitive.getInvertedAbstract() != null) {
            dto.setPaperAbstract(detailFromCognitive.getInvertedAbstract().toAbstract());
        }

        if (dto.getUrls().isEmpty() && !detailFromCognitive.getSources().isEmpty()) {
            List<PaperUrlDto> urls = detailFromCognitive.getSources()
                    .stream()
                    .map(s -> {
                        PaperUrlDto urlDto = new PaperUrlDto();
                        urlDto.setUrl(s.getUrl());
                        return urlDto;
                    })
                    .collect(Collectors.toList());

            dto.setUrlCount(urls.size());
            dto.setUrls(urls);
        }

        if (!StringUtils.hasText(dto.getDoi())) {
            dto.setDoi(detailFromCognitive.getDoi());
        }

        return dto;
    }

    private long[] getPagedReferences(EvaluateResponseDto.Entity origin, Pageable pageable) {
        long[] referenceIds = origin.getReferences();

        return Arrays.copyOfRange(
                referenceIds,
                pageable.getOffset() >= referenceIds.length ? 0 : pageable.getOffset(),
                Math.min(pageable.getOffset() + pageable.getPageSize(), referenceIds.length));
    }

    private String buildPaperIdMatchQuery(long cognitivePaperId) {
        return "And(Ty='0',Id=" + cognitivePaperId + ")";
    }

    private String buildPaperIdsMatchQuery(long[] cognitivePaperIds) {
        String subQuery = Arrays.stream(cognitivePaperIds)
                .boxed()
                .map(ref -> "Id=" + ref)
                .collect(Collectors.joining(","));
        return "And(Ty='0',OR(" + subQuery + "))";
    }

    private String buildCitedMatchQuery(long cognitivePaperId) {
        return "And(Ty='0',RId=" + cognitivePaperId + ")";
    }

    private String buildTitleAndYearMatchQuery(List<PaperDto> dtos) {
        String query = dtos.stream()
                .map(dto -> {
                    String normalized = TextUtils.normalize(dto.getTitle());
                    String titleExpr = "Ti=='" + normalized + "'";

                    String yearExpr = "Y=" + dto.getYear();

                    return "And(" + titleExpr + "," + yearExpr + ")";
                })
                .collect(Collectors.joining(","));

        return "Or(" + query + ")";
    }

}
