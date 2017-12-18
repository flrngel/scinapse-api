package network.pluto.absolute.controller;

import network.pluto.absolute.security.TokenHelper;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.EmailVerificationService;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;

@RestController
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;
    private final MemberService memberService;
    private final TokenHelper tokenHelper;

    @Autowired
    public EmailVerificationController(EmailVerificationService emailVerificationService,
                                       MemberService memberService,
                                       TokenHelper tokenHelper) {
        this.emailVerificationService = emailVerificationService;
        this.memberService = memberService;
        this.tokenHelper = tokenHelper;
    }

    @RequestMapping(value = "/email-verification", method = RequestMethod.GET)
    public Result verify(HttpServletResponse response,
                         @ApiIgnore JwtUser user,
                         @RequestParam String token) {
        // verify email
        emailVerificationService.verify(token);

        if (user != null) {
            // jwt token role update if member already signed in
            Member member = memberService.getMember(user.getId());

            if (member.getAuthorities() == null) {
                throw new InsufficientAuthenticationException("Member has no roles assigned");
            }

            String jws = tokenHelper.generateToken(member);
            tokenHelper.addCookie(response, jws);
        }

        return Result.success();
    }
}