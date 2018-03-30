package network.pluto.absolute.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import network.pluto.absolute.dto.cognitive.CalcHistogramResponseDto;
import network.pluto.absolute.dto.cognitive.EvaluateResponseDto;
import network.pluto.absolute.dto.cognitive.InterpretResponseDto;
import network.pluto.absolute.error.ExternalApiCallException;
import network.pluto.absolute.util.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CognitivePaperService {

    private static final String DEFAULT_ATTRIBUTES = "Id";

    private final RestTemplate restTemplate;

    @Value("${pluto.ms.cognitive.uri}")
    private String cognitiveUri;

    private String interpretPath = "/interpret";
    private String evaluatePath = "/evaluate";
    private String calchistogramPath = "/calchistogram";

    @Value("${pluto.ms.cognitive.subscription.key}")
    private String cognitiveSubscriptionKey;

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

        return responseEntity.getBody().getRecommendQuery();
    }

    public Page<Long> search(String query, Pageable pageable) {
        URI uri = buildUri(evaluatePath);

        LinkedMultiValueMap<String, Object> body = buildRequestBody(query, DEFAULT_ATTRIBUTES, pageable.getOffset(), pageable.getPageSize());
        HttpEntity<Object> httpEntity = buildHttpEntity(body);

        EvaluateResponseDto response = getResponse(uri, httpEntity);

        List<Long> dtos = response.getEntities().stream()
                .map(EvaluateResponseDto.Entity::getCognitivePaperId)
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

}
