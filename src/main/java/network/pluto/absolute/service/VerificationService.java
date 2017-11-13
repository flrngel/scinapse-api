package network.pluto.absolute.service;

import network.pluto.absolute.error.BadRequestException;
import network.pluto.bibliotheca.enums.AuthorityName;
import network.pluto.bibliotheca.models.Authority;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.Verification;
import network.pluto.bibliotheca.repositories.AuthorityRepository;
import network.pluto.bibliotheca.repositories.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Component
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final JavaMailSender javaMailSender;
    private final MemberService memberService;
    private final AuthorityRepository authorityRepository;

    @Autowired
    public VerificationService(VerificationRepository verificationRepository, JavaMailSender javaMailSender, MemberService memberService, AuthorityRepository authorityRepository) {
        this.verificationRepository = verificationRepository;
        this.javaMailSender = javaMailSender;
        this.memberService = memberService;
        this.authorityRepository = authorityRepository;
    }

    @Value("${pluto.server.web.url}")
    private String webServerUrl;

    @Transactional
    public void sendVerification(Member member) {
        String token = UUID.randomUUID().toString();
        Verification verification = new Verification();
        verification.setToken(token);
        verification.setMemberId(member.getId());
        verificationRepository.save(verification);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@pluto.network");
        message.setTo(member.getEmail());
        message.setSubject("Welcome to Pluto! Please verify your email address");
        message.setText("Hello, " + member.getName() + ".\n\nPlease visit below link to verify your email address:\n\n" + webServerUrl + "/verification?token=" + token + "\n\nThank you for joining Pluto Network!");

        new Thread(() -> javaMailSender.send(message)).start();
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
