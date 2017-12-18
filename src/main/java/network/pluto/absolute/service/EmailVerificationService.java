package network.pluto.absolute.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.bibliotheca.enums.AuthorityName;
import network.pluto.bibliotheca.models.Authority;
import network.pluto.bibliotheca.models.EmailVerification;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.repositories.AuthorityRepository;
import network.pluto.bibliotheca.repositories.EmailVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final AuthorityRepository authorityRepository;

    @Autowired
    public EmailVerificationService(EmailVerificationRepository emailVerificationRepository, AuthorityRepository authorityRepository) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.authorityRepository = authorityRepository;
    }

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

        SendEmailRequest request = new SendEmailRequest()
                .withDestination(new Destination().withToAddresses(member.getEmail()))
                .withMessage(new Message()
                        .withSubject(new Content()
                                .withData("Welcome to Pluto! Please verify your email address"))
                        .withBody(new Body()
                                .withText(new Content()
                                        .withData("Hello, " + member.getName() + ".\n\n" +
                                                "Please visit below link to verify your email address:\n" +
                                                webEmailVerificationUrl + "?email=" + member.getEmail() + "&token=" + token + "\n\n" +
                                                "Thank you for joining Pluto Network!"))))
                .withSource("no-reply@pluto.network");

        client.sendEmailAsync(request);
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
    }
}
