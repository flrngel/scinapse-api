package io.scinapse.api.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sendgrid.*;
import io.scinapse.domain.data.scinapse.model.Member;
import io.scinapse.domain.data.scinapse.model.PasswordResetToken;
import io.scinapse.domain.data.scinapse.repository.PasswordResetTokenRepository;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.error.ExternalApiCallException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository repository;
    private final MemberService memberService;

    private final SendGrid sendGrid;

    @Value("${pluto.server.web.url.reset-password}")
    private String webResetPasswordUrl;

    @Value("${pluto.server.email.sg.template.reset-password}")
    private String resetPasswordTemplate;

    @Transactional
    public void generateToken(Member member) {
        String token = UUID.randomUUID().toString();

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setMember(member);
        PasswordResetToken saved = repository.save(passwordResetToken);

        sendEmail(saved);
    }

    @Transactional
    public void resetPassword(String token, String password) {
        PasswordResetToken one = repository.findOne(token);
        if (one == null) {
            throw new BadRequestException("Invalid Password Reset Token");
        }
        if (one.getCreatedAt().isBefore(OffsetDateTime.now().minusDays(1))) {
            throw new BadRequestException("Password Reset Token is outdated");
        }
        if (one.isUsed()) {
            throw new BadRequestException("Password Reset Token is already used");
        }

        memberService.updatePassword(one.getMember(), password);
        one.setUsed(true);
    }

    private void sendEmail(PasswordResetToken token) {
        Mail mail = new Mail();
        mail.addCategory("reset_password");
        mail.setTemplateId(resetPasswordTemplate);
        mail.setFrom(getNoReplyFrom());
        mail.setReplyTo(getReplyTo());

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(token.getMember().getEmail()));

        PasswordResetData data = new PasswordResetData(token.getMember().getFullName(), webResetPasswordUrl + "?token=" + token.getToken());
        personalization.addDynamicTemplateData("data", data);

        mail.addPersonalization(personalization);

        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sendGrid.api(request);
        } catch (IOException e) {
            throw new ExternalApiCallException("Unable to send email : " + e.getMessage());
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

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class PasswordResetData {
        @JsonProperty
        String username;

        @JsonProperty
        String resetPasswordUrl;

        PasswordResetData(String username, String resetPasswordUrl) {
            this.username = username;
            this.resetPasswordUrl = resetPasswordUrl;
        }
    }

}
