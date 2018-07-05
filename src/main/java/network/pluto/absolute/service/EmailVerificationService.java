package network.pluto.absolute.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClientBuilder;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.SendTemplatedEmailRequest;
import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import network.pluto.absolute.enums.AuthorityName;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.models.Authority;
import network.pluto.absolute.models.EmailVerification;
import network.pluto.absolute.models.Member;
import network.pluto.absolute.repositories.AuthorityRepository;
import network.pluto.absolute.repositories.EmailVerificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@XRayEnabled
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final AuthorityRepository authorityRepository;
    private final ObjectMapper objectMapper;

    @Value("${pluto.server.web.url.email-verification}")
    private String webEmailVerificationUrl;

    private AmazonSimpleEmailServiceAsync client = AmazonSimpleEmailServiceAsyncClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

    @Transactional
    public void sendVerification(Member member) {
        String token = UUID.randomUUID().toString();
        EmailVerification emailVerification = new EmailVerification();
        emailVerification.setToken(token);
        emailVerification.setMember(member);
        emailVerificationRepository.save(emailVerification);

        sendVerificationEmail(member, token);
    }

    private void sendVerificationEmail(Member member, String token) {
        String templateData;
        try {
            VerifyEmailData data = new VerifyEmailData(member.getName(), webEmailVerificationUrl + "?email=" + member.getEmail() + "&token=" + token);
            templateData = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json Processing Exception", e);
        }

        SendTemplatedEmailRequest request = new SendTemplatedEmailRequest()
                .withDestination(new Destination().withToAddresses(member.getEmail()))
                .withSource("no-reply@pluto.network")
                .withTemplate("verify-email")
                .withTemplateData(templateData);

        client.sendTemplatedEmailAsync(request);
    }

    @Transactional
    public void verify(String token) {
        EmailVerification emailVerification = emailVerificationRepository.findByToken(token);
        if (emailVerification == null) {
            throw new BadRequestException("Verification failed.");
        }

        Member member = emailVerification.getMember();
        if (member.isEmailVerified()) {
            // member email already verified
            return;
        }

        Authority authority = authorityRepository.findByName(AuthorityName.ROLE_USER);
        member.setAuthorities(Collections.singletonList(authority));
        member.setEmailVerified(true);

        sendSignUpWelcomeEmail(member);
    }

    public void sendSignUpWelcomeEmail(Member member) {
        String templateData;
        try {
            SignUpWelcomeData data = new SignUpWelcomeData(member.getName());
            templateData = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json Processing Exception", e);
        }

        SendTemplatedEmailRequest request = new SendTemplatedEmailRequest()
                .withDestination(new Destination().withToAddresses(member.getEmail()))
                .withSource("no-reply@pluto.network")
                .withTemplate("sign-up-welcome")
                .withTemplateData(templateData);

        client.sendTemplatedEmailAsync(request);
    }

    public static class VerifyEmailData {
        @JsonProperty
        String name;

        @JsonProperty("verify-email-url")
        String verifyEmailUrl;

        public VerifyEmailData(String name, String verifyEmailUrl) {
            this.name = name;
            this.verifyEmailUrl = verifyEmailUrl;
        }
    }

    public static class SignUpWelcomeData {
        @JsonProperty
        String name;

        public SignUpWelcomeData(String name) {
            this.name = name;
        }
    }

}
