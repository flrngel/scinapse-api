package network.pluto.absolute.controller;

import com.google.common.base.Strings;
import network.pluto.absolute.dto.LoginDto;
import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.security.AuthRequest;
import network.pluto.absolute.security.TokenHelper;
import network.pluto.absolute.security.TokenInvalidException;
import network.pluto.absolute.security.TokenState;
import network.pluto.absolute.service.LoginUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenHelper tokenHelper;
    private final UserDetailsService userDetailsService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, TokenHelper tokenHelper, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.tokenHelper = tokenHelper;
        this.userDetailsService = userDetailsService;
    }

    @Value("${jwt.cookie}")
    private String cookie;

    @Value("${jwt.expires-in}")
    private int expireIn;

    @RequestMapping(value = "/auth/login", method = RequestMethod.POST)
    public LoginDto login(@RequestBody AuthRequest authRequest, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(token);

        LoginUserDetails user = (LoginUserDetails) authentication.getPrincipal();

        String jws = tokenHelper.generateToken(user.getMember());

        Cookie authCookie = new Cookie(cookie, jws);
        authCookie.setPath("/");
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(expireIn);
        response.addCookie(authCookie);

        MemberDto memberDto = MemberDto.fromEntity(user.getMember());
        return LoginDto.of(true, memberDto);
    }

    @RequestMapping(value = "/auth/login", method = RequestMethod.GET)
    public LoginDto login(HttpServletRequest request) {
        String authToken = tokenHelper.getToken(request);

        if (authToken == null) {
            return LoginDto.of(false, null);
        }

        String username = tokenHelper.getUsernameFromToken(authToken);
        if (Strings.isNullOrEmpty(username)) {
            throw new TokenInvalidException("invalid token", "username not exists");
        }

        LoginUserDetails user = (LoginUserDetails) userDetailsService.loadUserByUsername(username);
        MemberDto memberDto = MemberDto.fromEntity(user.getMember());
        return LoginDto.of(true, memberDto);
    }

    @RequestMapping(value = "/auth/refresh", method = RequestMethod.GET)
    public TokenState refresh(HttpServletRequest request, HttpServletResponse response) {
        String authToken = tokenHelper.getToken(request);

        if (authToken != null && tokenHelper.canTokenBeRefreshed(authToken)) {
            String refreshedToken = tokenHelper.refreshToken(authToken);

            Cookie authCookie = new Cookie(cookie, refreshedToken);
            authCookie.setPath("/");
            authCookie.setHttpOnly(true);
            authCookie.setMaxAge(expireIn);
            response.addCookie(authCookie);

            return new TokenState(refreshedToken, System.currentTimeMillis() + expireIn * 1000);
        } else {
            throw new RuntimeException("token expired.");
        }
    }
}
