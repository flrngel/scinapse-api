package network.pluto.absolute.controller;

import network.pluto.absolute.dto.LoginDto;
import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.dto.OAuthAuthorizeUriDto;
import network.pluto.absolute.dto.OAuthRequest;
import network.pluto.absolute.dto.oauth.OauthUserDto;
import network.pluto.absolute.enums.OAuthVendor;
import network.pluto.absolute.facade.MemberFacade;
import network.pluto.absolute.facade.OauthFacade;
import network.pluto.absolute.security.LoginRequest;
import network.pluto.absolute.security.TokenHelper;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AuthController {

    private final TokenHelper tokenHelper;
    private final MemberService memberService;
    private final MemberFacade memberFacade;
    private final BCryptPasswordEncoder encoder;
    private final OauthFacade oauthFacade;

    @Autowired
    public AuthController(TokenHelper tokenHelper,
                          MemberService memberService,
                          MemberFacade memberFacade,
                          BCryptPasswordEncoder encoder,
                          OauthFacade oauthFacade) {
        this.tokenHelper = tokenHelper;
        this.memberService = memberService;
        this.memberFacade = memberFacade;
        this.encoder = encoder;
        this.oauthFacade = oauthFacade;
    }

    @RequestMapping(value = "/auth/login", method = RequestMethod.GET)
    public LoginDto login(HttpServletResponse response, @ApiIgnore JwtUser user) {
        LoginDto loginDto = new LoginDto(false, null, null);

        if (user == null) {
            tokenHelper.removeCookie(response);
            return loginDto;
        }

        Member member = memberService.findMember(user.getId());
        if (member == null) {
            tokenHelper.removeCookie(response);
            return loginDto;
        }

        // now token is valid
        loginDto.setLoggedIn(true);
        loginDto.setToken(user.getToken());
        loginDto.setMember(new MemberDto(member));

        return loginDto;
    }

    @RequestMapping(value = "/auth/login", method = RequestMethod.POST)
    public LoginDto login(HttpServletResponse response, @RequestBody LoginRequest request) {
        if (StringUtils.isEmpty(request.getEmail()) || StringUtils.isEmpty(request.getPassword())) {
            throw new AuthenticationServiceException("Username or Password not provided");
        }

        Member member = memberService.getByEmail(request.getEmail(), true);
        if (member == null) {
            throw new UsernameNotFoundException("Member not found: " + request.getEmail());
        }

        if (!encoder.matches(request.getPassword(), member.getPassword())) {
            throw new BadCredentialsException("Authentication Failed. Username or Password not valid.");
        }

        if (member.getAuthorities() == null) {
            throw new InsufficientAuthenticationException("Member has no roles assigned");
        }

        String jws = tokenHelper.generateToken(member);
        tokenHelper.addCookie(response, jws);

        MemberDto memberDto = new MemberDto(member);
        return new LoginDto(true, jws, memberDto);
    }

    @RequestMapping(value = "/auth/logout", method = RequestMethod.POST)
    public Result logout(HttpServletResponse response) {

        // remove jwt cookie
        tokenHelper.removeCookie(response);

        // clear context
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.clearContext();

        return Result.success();
    }

    @RequestMapping(value = "/auth/oauth/authorize-uri", method = RequestMethod.GET)
    public OAuthAuthorizeUriDto getAuthorizeUri(@RequestParam OAuthVendor vendor,
                                                @RequestParam(required = false) String redirectUri) {
        URI uri = oauthFacade.getAuthorizeUri(vendor, redirectUri);

        OAuthAuthorizeUriDto dto = new OAuthAuthorizeUriDto();
        dto.setVendor(vendor);
        dto.setUri(uri);

        return dto;
    }

    @RequestMapping(value = "/auth/oauth/exchange", method = RequestMethod.POST)
    public OauthUserDto exchange(@RequestBody @Valid OAuthRequest request) {
        return oauthFacade.exchange(request.getVendor(), request.getCode(), request.getRedirectUri());
    }

    @RequestMapping(value = "/auth/oauth/login", method = RequestMethod.POST)
    public LoginDto login(HttpServletResponse response,
                          @RequestBody @Valid OAuthRequest request) {
        Member member = oauthFacade.findMember(request.getVendor(), request.getCode(), request.getRedirectUri());
        if (member == null) {
            throw new BadCredentialsException("Authentication Failed. Member not existence.");
        }

        String jws = tokenHelper.generateToken(member);
        tokenHelper.addCookie(response, jws);

        MemberDto memberDto = new MemberDto(member);
        return new LoginDto(true, jws, memberDto);
    }

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public Object hello() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "hello, world.");
        return result;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public Object user(@ApiIgnore JwtUser user) {
        return getMessage(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public Object admin(@ApiIgnore JwtUser user) {
        return getMessage(user);
    }

    private Object getMessage(JwtUser user) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "hello, " + user.getName() + ".");
        result.put("id", user.getId());
        result.put("email", user.getEmail());
        result.put("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        result.put("name", user.getName());
        return result;
    }
}
