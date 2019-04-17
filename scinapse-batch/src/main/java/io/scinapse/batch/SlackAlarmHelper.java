package io.scinapse.batch;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class SlackAlarmHelper {

    @Value("${pluto.server.slack.batch.email.url}")
    private String slackUrl;

    public void sendSlackAlarm(String message) {
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

}
