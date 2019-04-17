package io.scinapse.batch.job;

import com.sendgrid.*;
import io.scinapse.batch.SendAtUtil;
import io.scinapse.batch.SlackAlarmHelper;
import io.scinapse.domain.data.scinapse.model.Member;
import io.scinapse.domain.data.scinapse.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@JobScope
@Component
@RequiredArgsConstructor
public class SignUpD3EmailTasklet implements Tasklet {

    @Value("#{jobParameters[target_date]}")
    private String targetDate;

    @Value("${pluto.server.email.sg.template.sign-up-d3}")
    private String signUpD3EmailTemplate;

    @Value("${pluto.server.web.url}")
    private String webUrl;

    private final Environment environment;
    private final SendGrid sendGrid;
    private final SlackAlarmHelper helper;

    private final MemberRepository memberRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws ParseException {
        SendAtUtil.init();
        String[] profiles = environment.getActiveProfiles();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date parse = format.parse(targetDate);
        List<Member> members = memberRepository.findInDate(parse);

        helper.sendSlackAlarm("`" + Arrays.toString(profiles) + "` *Sign Up D3* Sending mails to " + members.size() + " users...");

        // send email
        int counter = 0;
        for (Member member : members) {
            try {
                sendEmail(member);
                counter++;
            } catch (Exception e) {
                log.error("Cannot send email.", e);
            }
        }

        helper.sendSlackAlarm("`" + Arrays.toString(profiles) + "` *Sign Up D3* Sent " + counter + " mails successfully!!!");

        return RepeatStatus.FINISHED;
    }

    private void sendEmail(Member member) {
        Mail mail = new Mail();
        mail.addCategory("sign-up-d3");
        mail.setTemplateId(signUpD3EmailTemplate);
        mail.setFrom(getNoReplyFrom());
        mail.setReplyTo(getReplyTo());

        ASM asm = new ASM();
        asm.setGroupId(9607);
        asm.setGroupsToDisplay(new int[] { 9607 });
        mail.setASM(asm);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(member.getEmail()));
        personalization.addDynamicTemplateData("username", member.getFullName());

        mail.addPersonalization(personalization);

        OffsetDateTime sendAt = SendAtUtil.getSendAt(member.getCreatedAt());
        log.info("Sending mail to {} at {} ...", member.getEmail(), sendAt);
        mail.setSendAt(sendAt.toEpochSecond());

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
        from.setEmail("team@pluto.network");
        from.setName("Scinapse");
        return from;
    }

    private Email getReplyTo() {
        Email from = new Email();
        from.setEmail("team@pluto.network");
        return from;
    }

}

