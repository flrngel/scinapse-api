package network.pluto.absolute.controller;

import com.google.common.base.Strings;
import network.pluto.absolute.dto.LoginDto;
import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.security.TokenExpiredException;
import network.pluto.absolute.security.TokenHelper;
import network.pluto.absolute.security.TokenInvalidException;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    @RequestMapping(value = "/auth/refresh", method = RequestMethod.POST)
    public LoginDto refresh(HttpServletRequest request, HttpServletResponse response) {
        String authToken = tokenHelper.getToken(request);

        if (authToken == null || !tokenHelper.canTokenBeRefreshed(authToken)) {
            throw new TokenExpiredException("expired token", authToken, "token has expired");
        }

        String username = tokenHelper.getUsernameFromToken(authToken);
        if (Strings.isNullOrEmpty(username)) {
            throw new TokenInvalidException("invalid token", authToken, "username not found");
        }

        String refreshedToken = tokenHelper.refreshToken(authToken);

        Cookie authCookie = new Cookie(cookie, refreshedToken);
        authCookie.setPath("/");
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(expireIn);
        response.addCookie(authCookie);

        Member member = memberService.findByEmail(username);
        MemberDto memberDto = new MemberDto(member);
        return new LoginDto(true, refreshedToken, memberDto);
    }

    @RequestMapping("/hello")
    public String hello() {
        return "hello, world.";
    }

    @RequestMapping("/admin")
    public String admin() {
        return "hello, admin.";
    }
}
