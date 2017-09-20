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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {

    private final TokenHelper tokenHelper;
    private final MemberService memberService;

    @Autowired
    public AuthController(TokenHelper tokenHelper, MemberService memberService) {
        this.tokenHelper = tokenHelper;
        this.memberService = memberService;
    }

    @Value("${jwt.cookie}")
    private String cookie;

    @Value("${jwt.expires-in}")
    private int expireIn;

    @RequestMapping(value = "/auth/login", method = RequestMethod.GET)
    public LoginDto login(HttpServletRequest request) {
        String authToken = tokenHelper.getToken(request);

        if (authToken == null) {
            return LoginDto.of(false, null, null);
        }

        String username = tokenHelper.getUsernameFromToken(authToken);
        if (Strings.isNullOrEmpty(username)) {
            throw new TokenInvalidException("invalid token", "username not exists");
        }

        Member member = memberService.findByEmail(username);
        MemberDto memberDto = MemberDto.fromEntity(member);
        return LoginDto.of(true, authToken, memberDto);
    }

    @RequestMapping(value = "/auth/refresh", method = RequestMethod.GET)
    public LoginDto refresh(HttpServletRequest request, HttpServletResponse response) {
        String authToken = tokenHelper.getToken(request);

        if (authToken != null && tokenHelper.canTokenBeRefreshed(authToken)) {
            String refreshedToken = tokenHelper.refreshToken(authToken);

            Cookie authCookie = new Cookie(cookie, refreshedToken);
            authCookie.setPath("/");
            authCookie.setHttpOnly(true);
            authCookie.setMaxAge(expireIn);
            response.addCookie(authCookie);

            String username = tokenHelper.getUsernameFromToken(authToken);
            if (Strings.isNullOrEmpty(username)) {
                throw new TokenInvalidException("invalid token", "username not exists");
            }

            Member member = memberService.findByEmail(username);
            MemberDto memberDto = MemberDto.fromEntity(member);
            return LoginDto.of(true, refreshedToken, memberDto);
        } else {
            throw new RuntimeException("token expired.");
        }
    }
}
