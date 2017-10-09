package network.pluto.absolute.controller;

import com.google.common.base.Strings;
import network.pluto.absolute.dto.LoginDto;
import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.security.TokenHelper;
import network.pluto.absolute.security.TokenInvalidException;
import network.pluto.absolute.security.jwt.JwtAuthenticationToken;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Authority;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
        result.put("message", "hello, " + token.getMember().getFullName() + ".");
        result.put("email", token.getMember().getEmail());
        result.put("roles", token.getMember().getAuthorities().stream().map(Authority::getName).collect(Collectors.toList()));
        result.put("fullName", token.getMember().getFullName());
        return result;
    }
}
