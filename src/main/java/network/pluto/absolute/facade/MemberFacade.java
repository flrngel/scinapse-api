package network.pluto.absolute.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.RequiredArgsConstructor;
import network.pluto.absolute.configuration.CacheName;
import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.security.TokenHelper;
import network.pluto.absolute.service.CommentService;
import network.pluto.absolute.service.EmailVerificationService;
import network.pluto.absolute.service.MemberService;
import network.pluto.absolute.service.PasswordResetService;
import network.pluto.bibliotheca.enums.AuthorityName;
import network.pluto.bibliotheca.models.Member;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;

@XRayEnabled
@Component
@RequiredArgsConstructor
public class MemberFacade {

    private final MemberService memberService;
    private final CommentService commentService;
    private final EmailVerificationService emailVerificationService;
    private final TokenHelper tokenHelper;
    private final OauthFacade oauthFacade;
    private final PasswordResetService passwordResetService;

    @Cacheable(CacheName.Member.GET_DETAIL)
    public MemberDto getDetail(long memberId) {
        Member member = memberService.findMember(memberId);
        if (member == null) {
            throw new ResourceNotFoundException("Member not found");
        }

        long commentCount = commentService.getCount(member);

        MemberDto memberDto = new MemberDto(member);
        memberDto.setCommentCount(commentCount);

        return memberDto;
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

}
