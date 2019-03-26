package io.scinapse.batch.job;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Joiner;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sendgrid.*;
import io.scinapse.domain.data.academic.model.Author;
import io.scinapse.domain.data.academic.model.Paper;
import io.scinapse.domain.data.academic.model.PaperTopAuthor;
import io.scinapse.domain.data.academic.repository.PaperRepository;
import io.scinapse.domain.data.scinapse.repository.CollectionRepository;
import io.scinapse.domain.data.scinapse.repository.CollectionRepositoryImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectionEmailTasklet implements Tasklet {

    private final CollectionRepository collectionRepository;
    private final PaperRepository paperRepository;

    private final Environment environment;

    private final SendGrid sendGrid;

    @Value("${pluto.server.email.sg.template.retention-collection}")
    private String collectionEmailTemplate;

    @Value("${pluto.server.web.url}")
    private String webUrl;

    @Value("${pluto.server.slack.batch.email.url}")
    private String slackUrl;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        String[] profiles = environment.getActiveProfiles();

        List<CollectionRepositoryImpl.CollectionEmailDataWrapper> data = collectionRepository.getCollectionEmailData();
        List<CollectionEmailData> emailDataList = convertData(data);

        sendSlackAlarm("`" + Arrays.toString(profiles) + "` Sending mails to " + emailDataList.size() + " users...");

        // send email
        emailDataList.forEach(this::sendEmail);

        sendSlackAlarm("`" + Arrays.toString(profiles) + "` Sent " + emailDataList.size() + " mails successfully!!!");

        return RepeatStatus.FINISHED;
    }

    private void sendSlackAlarm(String message) {
        log.info("------------------------------------------------");
        log.info(message);
        log.info("------------------------------------------------");

        Map<String, Object> slackMessage = new HashMap<>();
        slackMessage.put("text", message);
        try {
            Unirest.post(slackUrl)
                    .body(slackMessage)
                    .asString();
        } catch (UnirestException e) {
            log.error("Cannot send slack alarm.", e);
        }
    }

    private List<CollectionEmailData> convertData(List<CollectionRepositoryImpl.CollectionEmailDataWrapper> data) {
        Map<Long, Paper> paperMap = getPaperMap(data);
        return data.stream()
                .collect(Collectors.groupingBy(CollectionRepositoryImpl.CollectionEmailDataWrapper::getMemberId))
                .values()
                .stream()
                .filter(c -> !CollectionUtils.isEmpty(c))
                .map(dataList -> {
                    CollectionRepositoryImpl.CollectionEmailDataWrapper wrapper = dataList.get(0);

                    CollectionEmailData emailData = new CollectionEmailData();

                    String username = Joiner.on(" ").skipNulls().join(wrapper.getFirstName(), wrapper.getLastName());
                    emailData.setEmail(wrapper.getEmail());
                    emailData.setUsername(username);
                    emailData.setCount(wrapper.getCount());

                    Map<Long, List<CollectionRepositoryImpl.CollectionEmailDataWrapper>> collect = dataList.stream()
                            .collect(Collectors.groupingBy(CollectionRepositoryImpl.CollectionEmailDataWrapper::getCollectionId));

                    collect.forEach((collectionId, list) -> {
                        CollectionData collectionData = getCollectionData(paperMap, collectionId, list);
                        emailData.getCollections().add(collectionData);
                    });

                    return emailData;
                })
                .collect(Collectors.toList());
    }

    private Map<Long, Paper> getPaperMap(List<CollectionRepositoryImpl.CollectionEmailDataWrapper> data) {
        Set<Long> paperIds = data.stream().map(CollectionRepositoryImpl.CollectionEmailDataWrapper::getPaperId).collect(Collectors.toSet());
        return paperRepository.findByIdIn(paperIds)
                .stream()
                .collect(Collectors.toMap(
                        Paper::getId,
                        Function.identity()
                ));
    }

    private void sendEmail(CollectionEmailData data) {
        Mail mail = new Mail();
        mail.addCategory("retention_collection");
        mail.setTemplateId(collectionEmailTemplate);
        mail.setFrom(getNoReplyFrom());
        mail.setReplyTo(getReplyTo());

        ASM asm = new ASM();
        asm.setGroupId(9250);
        asm.setGroupsToDisplay(new int[] { 9250 });
        mail.setASM(asm);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(data.getEmail()));
        personalization.addDynamicTemplateData("data", data);

        mail.addPersonalization(personalization);

        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sendGrid.api(request);
        } catch (IOException e) {
            throw new RuntimeException("Unable to send email : " + e.getMessage());
        }
    }

    private Email getNoReplyFrom() {
        Email from = new Email();
        from.setEmail("no-reply@scinapse.io");
        from.setName("Scinapse");
        return from;
    }

    private Email getReplyTo() {
        Email from = new Email();
        from.setEmail("team@pluto.network");
        return from;
    }

    private CollectionData getCollectionData(Map<Long, Paper> paperMap, Long collectionId, List<CollectionRepositoryImpl.CollectionEmailDataWrapper> list) {
        List<PaperData> paperDataList = list.stream()
                .map(l -> {
                    PaperData paperData = new PaperData();
                    paperData.setUrl(webUrl + "/papers/" + l.getPaperId());
                    paperData.setTitle(l.getPaperTitle());
                    paperData.setYear(l.getPaperYear());

                    Paper paper = paperMap.get(l.getPaperId());
                    if (paper == null) {
                        return paperData;
                    }

                    if (paper.getJournal() != null) {
                        paperData.setJournalTitle(paper.getJournal().getTitle());
                    }

                    if (!CollectionUtils.isEmpty(paper.getAuthors())) {
                        String authorName = paper.getAuthors()
                                .stream()
                                .map(PaperTopAuthor::getAuthor)
                                .map(Author::getName)
                                .filter(Objects::nonNull)
                                .collect(Collectors.joining(", "));
                        paperData.setAuthorName(authorName);
                    }

                    return paperData;
                })
                .collect(Collectors.toList());

        CollectionData collectionData = new CollectionData();
        collectionData.setUrl(webUrl + "/collections/" + collectionId);
        collectionData.setTitle(list.get(0).getCollectionTitle());
        collectionData.getPapers().addAll(paperDataList);
        return collectionData;
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Getter
    @Setter
    private class CollectionEmailData {
        String username;
        String email;
        int count;
        List<CollectionData> collections = new ArrayList<>();
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Getter
    @Setter
    private class CollectionData {
        String url;
        String title;
        List<PaperData> papers = new ArrayList<>();
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Getter
    @Setter
    private class PaperData {
        String url;
        String title;
        String authorName;
        String journalTitle;
        Integer year;
    }

}
