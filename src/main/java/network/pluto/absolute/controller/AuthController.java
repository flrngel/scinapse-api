package network.pluto.absolute.controller;

import com.google.common.base.Strings;
import network.pluto.absolute.dto.LoginDto;
import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.security.TokenHelper;
import network.pluto.absolute.security.TokenInvalidException;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AuthController {

    @Value("${jwt.cookie}")
    private String cookie;

    @Value("${jwt.expires-in}")
    private int expireIn;

    private final TokenHelper tokenHelper;
    private final MemberService memberService;

    @Autowired
    public AuthController(TokenHelper tokenHelper, MemberService memberService) {
        this.tokenHelper = tokenHelper;
        this.memberService = memberService;
    }

    @RequestMapping(value = "/auth/login", method = RequestMethod.GET)
    public LoginDto login(HttpServletRequest request) {
        String authToken = tokenHelper.getToken(request);

        if (authToken == null) {
            return new LoginDto(false, null, null);
        }

        String username = tokenHelper.getUsernameFromToken(authToken);
        if (Strings.isNullOrEmpty(username)) {
            throw new TokenInvalidException("invalid token", authToken, "username not found");
        }

        Member member = memberService.findByEmail(username);
        MemberDto memberDto = new MemberDto(member);
        return new LoginDto(true, authToken, memberDto);
    }

    @RequestMapping("/hello")
    public String hello() {
        return "hello, world.";
    }

    @RequestMapping("/user")
    public String user() {
        return "hello, user.";
    }

    @RequestMapping("/admin")
    public String admin() {
        return "hello, admin.";
    }
}
