package network.pluto.absolute.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.bibliotheca.enums.AuthorityName;
import network.pluto.bibliotheca.models.Authority;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.Verification;
import network.pluto.bibliotheca.repositories.AuthorityRepository;
import network.pluto.bibliotheca.repositories.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Service
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final MemberService memberService;
    private final AuthorityRepository authorityRepository;

    @Autowired
    public VerificationService(VerificationRepository verificationRepository, MemberService memberService, AuthorityRepository authorityRepository) {
        this.verificationRepository = verificationRepository;
        this.memberService = memberService;
        this.authorityRepository = authorityRepository;
    }

    @Value("${pluto.server.web.url}")
    private String webServerUrl;

    private AmazonSimpleEmailServiceAsync client = AmazonSimpleEmailServiceAsyncClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

    @Transactional
    public void sendVerification(Member member) {
        String token = UUID.randomUUID().toString();
        Verification verification = new Verification();
        verification.setToken(token);
        verification.setMemberId(member.getId());
        verificationRepository.save(verification);

        SendEmailRequest request = new SendEmailRequest()
                .withDestination(new Destination().withToAddresses(member.getEmail()))
                .withMessage(new Message()
                        .withSubject(new Content()
                                .withData("Welcome to Pluto! Please verify your email address"))
                        .withBody(new Body()
                                .withText(new Content()
                                        .withData("Hello, " + member.getName() + ".\n\n" +
                                                "Please visit below link to verify your email address:\n\n"
                                                + webServerUrl + "/verification?token=" + token + "\n\n" +
                                                "Thank you for joining Pluto Network!"))))
                .withSource("no-reply@pluto.network");

        client.sendEmailAsync(request);
    }

    @Transactional
    public void verify(String token) {
        Verification verification = verificationRepository.findByToken(token);
        if (verification == null) {
            throw new BadRequestException("Verification failed.");
        }

        long memberId = verification.getMemberId();
        Member member = memberService.getMember(memberId);
        Authority authority = authorityRepository.findByName(AuthorityName.ROLE_USER);
        member.setAuthorities(Collections.singletonList(authority));
    }
}
