package io.scinapse.api.service;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemUtils;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.dto.PaperImageDto;
import io.scinapse.api.model.mag.Paper;
import io.scinapse.api.model.mag.PaperUrl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@XRayEnabled
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PaperPdfImageService {

    private AmazonSQSAsync sqsAsync = AmazonSQSAsyncClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
    private AmazonDynamoDBAsync dbAsync = AmazonDynamoDBAsyncClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
    private DynamoDB dynamoDB = new DynamoDB(dbAsync);

    @Value("${pluto.server.dynamo.table.paper-pdf-images}")
    private String paperPdfImageTableName;

    @Value("${pluto.server.sqs.url.figure-extract}")
    private String figureExtractQueueUrl;

    @Value("${pluto.server.web.url.asset}")
    private String plutoAssetUrl;

    public Optional<List<PaperImageDto>> getPdfImages(long paperId) {
        Table table = dynamoDB.getTable(paperPdfImageTableName);
        Item pdfImageRecord = table.getItem("paper_id", String.valueOf(paperId));

        return Optional.ofNullable(pdfImageRecord)
                .map(PaperPdfImage::new)
                .map(pdfImage -> {
                    if (pdfImage.getProcessStatus() != ProcessStatus.DONE) {
                        return new ArrayList<>();
                    }

                    return Optional.ofNullable(pdfImage.getPaperImages())
                            .map(pdfImages -> pdfImages
                                    .stream()
                                    .map(image -> {
                                        if (StringUtils.isBlank(image)) {
                                            return null;
                                        }
                                        PaperImageDto imageDto = new PaperImageDto();
                                        imageDto.setPaperId(pdfImage.getPaperId());
                                        imageDto.setImageUrl(plutoAssetUrl + "/" + StringUtils.trim(image));
                                        return imageDto;
                                    })
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList()))
                            .orElse(new ArrayList<>());
                });
    }

    public void extractPdfImagesAsync(Paper paper) {
        dbAsync.putItemAsync(createPutRequest(paper), new AsyncHandler<PutItemRequest, PutItemResult>() {
            @Override
            public void onError(Exception exception) {
                log.error("Failed to put a new record into {}.", paperPdfImageTableName, exception);
            }

            @Override
            public void onSuccess(PutItemRequest request, PutItemResult putItemResult) {
                AttributeValue value = request.getItem().get("paper_id");
                String paperIdString = value.getS();
                if (StringUtils.isBlank(paperIdString)) {
                    log.error("Something went wrong. Cannot retrieve primary key. Request: {}", request);
                    return;
                }

                sendMessageAsync(paperIdString);
            }
        });
    }

    private void sendMessageAsync(String paperIdString) {
        String paperIdJson = "{\"paper_id\":\"" + paperIdString + "\"}";
        SendMessageRequest messageRequest = new SendMessageRequest()
                .withQueueUrl(figureExtractQueueUrl)
                .withMessageBody(paperIdJson);

        sqsAsync.sendMessageAsync(messageRequest);
    }

    private PutItemRequest createPutRequest(Paper paper) {
        Item newRecord = new Item()
                .withPrimaryKey("paper_id", String.valueOf(paper.getId()))
                .withStringSet("paper_urls", paper.getPaperUrls().stream().map(PaperUrl::getSourceUrl).collect(Collectors.toSet()))
                .with("process_status", ProcessStatus.PENDING.getStatus());

        return new PutItemRequest()
                .withTableName(paperPdfImageTableName)
                .withConditionExpression("attribute_not_exists(paper_id)")
                .withItem(ItemUtils.toAttributeValues(newRecord));
    }

    @Getter
    @Setter
    public static class PaperPdfImage {
        private long paperId;
        private Set<String> paperUrls;
        private Set<String> paperImages;
        private ProcessStatus processStatus;

        public PaperPdfImage(Item record) {
            this.paperId = Long.parseLong(record.getString("paper_id"));
            this.paperUrls = record.getStringSet("paper_urls");
            this.paperImages = record.getStringSet("paper_images");
            this.processStatus = ProcessStatus.convert(record.getString("process_status"));
        }
    }

    @Getter
    public enum ProcessStatus {
        PENDING("pending"),
        FAILED("failed"),
        DONE("done");

        private String status;

        ProcessStatus(String status) {
            this.status = status;
        }

        public static ProcessStatus convert(String status) {
            for (ProcessStatus value : ProcessStatus.values()) {
                if (value.getStatus().equals(status)) {
                    return value;
                }
            }
            return null;
        }
    }

}
