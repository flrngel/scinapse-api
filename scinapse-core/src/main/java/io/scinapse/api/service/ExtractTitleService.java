package io.scinapse.api.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@XRayEnabled
@Component
@RequiredArgsConstructor
public class ExtractTitleService {

    private final RestTemplate restTemplate;

    @Value("${pluto.server.fargate.citation-ner.endpoint}")
    private String citationNerEndpoint;

    public String extractCitationTitle(String citationText) {
        RequestData requestData = new RequestData(citationText);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RequestData> httpEntity = new HttpEntity<>(requestData, headers);
        ResponseEntity<ResponseData> responseEntity = restTemplate.exchange(citationNerEndpoint, HttpMethod.POST, httpEntity, ResponseData.class);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return null;
        }

        ResponseData responseData = responseEntity.getBody();
        if (CollectionUtils.isEmpty(responseData.getOutputs())) {
            // cannot find title from citation text
            return null;
        }

        return responseData.getOutputs().get(0);
    }

    @Getter
    @Setter
    private static class RequestData {
        private List<String> inputs = new ArrayList<>();

        public RequestData(String citationText) {
            this.inputs.add(citationText);
        }
    }

    @Getter
    @Setter
    private static class ResponseData {
        private List<String> outputs;
    }

}
