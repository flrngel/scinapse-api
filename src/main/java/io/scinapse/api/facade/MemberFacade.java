package io.scinapse.api.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.configuration.CacheName;
import io.scinapse.api.data.scinapse.model.Member;
import io.scinapse.api.dto.MemberDto;
import io.scinapse.api.enums.AuthorityName;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.security.TokenHelper;
import io.scinapse.api.security.jwt.JwtUser;
import io.scinapse.api.service.EmailVerificationService;
import io.scinapse.api.service.MemberService;
import io.scinapse.api.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;

@Transactional(readOnly = true)
@XRayEnabled
@Component
@RequiredArgsConstructor
public class MemberFacade {

    private final MemberService memberService;
    private final EmailVerificationService emailVerificationService;
    private final TokenHelper tokenHelper;
    private final OauthFacade oauthFacade;
    private final PasswordResetService passwordResetService;

    @Cacheable(CacheName.Member.GET_DETAIL)
    public MemberDto getDetail(long memberId) {
        Member member = memberService.findMember(memberId);
        if (member == null) {
            throw new ResourceNotFoundException("Member not found: " + memberId);
        }

        return new MemberDto(member);
    }

    @Transactional
    public Member create(HttpServletResponse response, MemberDto memberDto) {
        Member exist = memberService.findByEmail(memberDto.getEmail());
        if (exist != null) {
            throw new BadRequestException("Email already exists");
        }

        if (!StringUtils.hasText(memberDto.getPassword()) || memberDto.getPassword().length() < 8) {
            throw new BadRequestException("Password length must be greater than or equal to 8");
        }

        Member saved = memberService.saveMember(memberDto.toEntity());

        // send verification email
        emailVerificationService.sendVerification(saved);

        // auto login
        String jwt = tokenHelper.generateToken(saved, false);
        tokenHelper.addCookie(response, jwt);

        return saved;
    }

    @Transactional
    public Member createOauthMember(HttpServletResponse response, MemberDto memberDto) {
        Member exist = memberService.findByEmail(memberDto.getEmail());
        if (exist != null) {
            throw new BadRequestException("Email already exists");
        }

        if (memberDto.getOauth() == null) {
            throw new BadRequestException("Invalid Oauth Information: not exist");
        }

        if (oauthFacade.isConnected(memberDto.getOauth())) {
            throw new BadRequestException("Invalid Connection: already connected");
        }

        Member saved = memberService.saveMember(memberDto.toEntity());
        memberService.updateAuthority(saved, AuthorityName.ROLE_USER);

        oauthFacade.connect(memberDto.getOauth(), saved);

        // send welcome email
        emailVerificationService.sendSignUpWelcomeEmail(saved);

        // auto login
        String jwt = tokenHelper.generateToken(saved, true);
        tokenHelper.addCookie(response, jwt);

        return saved;
    }

    @Transactional
    public void generateToken(Member member) {
        passwordResetService.generateToken(member);
    }

    @Transactional
    public void resetPassword(String token, String password) {
        passwordResetService.resetPassword(token, password);
    }

    public Member loadMember(JwtUser user) {
        if (user == null) {
            throw new UsernameNotFoundException("User information does not exist.");
        }

        if (user.getId() <= 0) {
            throw new BadCredentialsException("Invalid user ID: " + user.getId());
        }

        Member member = memberService.findMember(user.getId());
        if (member == null) {
            throw new UsernameNotFoundException("User not found: " + user.getId());
        }

        return member;
    }

}
