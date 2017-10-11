package network.pluto.absolute.controller;

import com.google.common.base.Strings;
import network.pluto.absolute.dto.LoginDto;
import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.security.TokenHelper;
import network.pluto.absolute.security.jwt.JwtAuthenticationToken;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Authority;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AuthController {

    private final TokenHelper tokenHelper;
    private final MemberService memberService;

    @Autowired
    public AuthController(TokenHelper tokenHelper, MemberService memberService) {
        this.tokenHelper = tokenHelper;
        this.memberService = memberService;
    }

    @RequestMapping(value = "/auth/login", method = RequestMethod.GET)
    public LoginDto login(HttpServletRequest request, HttpServletResponse response) {
        LoginDto loginDto = new LoginDto(false, null, null);

        String authToken = tokenHelper.getToken(request);
        if (authToken == null) {
            return loginDto;
        }

        String username;
        try {
            username = tokenHelper.getUsernameFromToken(authToken);
        } catch (Exception e) {
            tokenHelper.deleteCookie(response);
            return loginDto;
        }

        if (Strings.isNullOrEmpty(username)) {
            tokenHelper.deleteCookie(response);
            return loginDto;
        }

        Member member = memberService.findByEmail(username);
        if (member == null) {
            tokenHelper.deleteCookie(response);
            return loginDto;
        }

        // now token is valid
        loginDto.setLoggedIn(true);
        loginDto.setToken(authToken);
        loginDto.setMember(new MemberDto(member));

        return loginDto;
    }

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public Object hello() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "hello, world.");
        return result;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public Object user(JwtAuthenticationToken token) {
        return getMessage(token);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public Object admin(JwtAuthenticationToken token) {
        return getMessage(token);
    }

    private Object getMessage(JwtAuthenticationToken token) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "hello, " + token.getMember().getName() + ".");
        result.put("email", token.getMember().getEmail());
        result.put("roles", token.getMember().getAuthorities().stream().map(Authority::getName).collect(Collectors.toList()));
        result.put("name", token.getMember().getName());
        return result;
    }
}
