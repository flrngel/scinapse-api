package io.scinapse.api.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntime;
import com.amazonaws.services.sagemakerruntime.AmazonSageMakerRuntimeClientBuilder;
import com.amazonaws.services.sagemakerruntime.model.InvokeEndpointRequest;
import com.amazonaws.services.sagemakerruntime.model.InvokeEndpointResult;
import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.scinapse.domain.util.JsonUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@XRayEnabled
@Component
@RequiredArgsConstructor
public class ExtractTitleService {

    @Value("${pluto.server.sagemaker.citation-ner.endpoint}")
    private String citationNerEndpoint;
    private AmazonSageMakerRuntime client = AmazonSageMakerRuntimeClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

    public String extractCitationTitle(String citationText) {
        RequestData requestData = new RequestData(citationText);

        byte[] bytes;
        try {
            bytes = JsonUtils.toBytes(requestData);
        } catch (JsonProcessingException e) {
            log.error("JSON serialization error occurs.", e);
            return null;
        }

        InvokeEndpointRequest request = new InvokeEndpointRequest()
                .withEndpointName(citationNerEndpoint)
                .withContentType(MediaType.APPLICATION_JSON_VALUE)
                .withBody(ByteBuffer.wrap(bytes));

        InvokeEndpointResult result = client.invokeEndpoint(request);
        ByteBuffer body = result.getBody();
        if (body == null) {
            return null;
        }

        ResponseData responseData;
       try {
            responseData = JsonUtils.fromBytes(body.array(), ResponseData.class);
        } catch (IOException e) {
            log.error("JSON deserialization error occurs.", e);
            return null;
        }

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
