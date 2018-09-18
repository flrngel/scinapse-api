package io.scinapse.api.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClientBuilder;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.SendTemplatedEmailRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.model.Member;
import io.scinapse.api.model.PasswordResetToken;
import io.scinapse.api.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository repository;
    private final MemberService memberService;
    private final ObjectMapper objectMapper;

    @Value("${pluto.server.web.url.reset-password}")
    private String webResetPasswordUrl;

    private AmazonSimpleEmailServiceAsync client = AmazonSimpleEmailServiceAsyncClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

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
        String templateData;
        try {
            PasswordResetData data = new PasswordResetData(token.getMember().getName(), webResetPasswordUrl + "?token=" + token.getToken());
            templateData = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json Processing Exception", e);
        }

        SendTemplatedEmailRequest request = new SendTemplatedEmailRequest()
                .withDestination(new Destination().withToAddresses(token.getMember().getEmail()))
                .withSource("no-reply@pluto.network")
                .withTemplate("reset-password")
                .withTemplateData(templateData);

        client.sendTemplatedEmailAsync(request);
    }

    public static class PasswordResetData {
        @JsonProperty
        String username;

        @JsonProperty("reset-password-url")
        String resetPasswordUrl;

        PasswordResetData(String username, String resetPasswordUrl) {
            this.username = username;
            this.resetPasswordUrl = resetPasswordUrl;
        }
    }

}
