package io.scinapse.api.controller;

import io.scinapse.api.dto.LoginDto;
import io.scinapse.api.dto.MemberDto;
import io.scinapse.api.dto.oauth.OAuthAuthorizeUriDto;
import io.scinapse.api.dto.oauth.OAuthRequest;
import io.scinapse.api.dto.oauth.OauthUserDto;
import io.scinapse.api.dto.response.Response;
import io.scinapse.api.dto.v2.OAuthConnection;
import io.scinapse.api.dto.v2.OAuthToken;
import io.scinapse.api.facade.OauthFacade;
import io.scinapse.api.security.LoginRequest;
import io.scinapse.api.security.TokenHelper;
import io.scinapse.api.security.jwt.JwtUser;
import io.scinapse.api.service.MemberService;
import io.scinapse.domain.data.scinapse.model.Member;
import io.scinapse.domain.enums.OauthVendor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.URI;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final TokenHelper tokenHelper;
    private final MemberService memberService;
    private final BCryptPasswordEncoder encoder;
    private final OauthFacade oauthFacade;

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
        loginDto.setOauthLoggedIn(user.isOauthLogin());
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

        if (member.getPassword() == null) {
            throw new BadCredentialsException("Authentication Failed. Oauth user did not register password.");
        }

        if (!encoder.matches(request.getPassword(), member.getPassword())) {
            throw new BadCredentialsException("Authentication Failed. Username or Password not valid.");
        }

        if (member.getAuthorities().isEmpty()) {
            throw new InsufficientAuthenticationException("Member has no roles assigned");
        }

        String jws = tokenHelper.generateToken(member, false);
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
    public OAuthAuthorizeUriDto getAuthorizeUri(@RequestParam OauthVendor vendor,
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

    @RequestMapping(value = "/auth/oauth/check", method = RequestMethod.POST)
    public Response<OAuthConnection> check(@RequestBody @Valid OAuthToken token) {
        OAuthConnection connection = oauthFacade.getConnection(token);
        return Response.success(connection);
    }

    @RequestMapping(value = "/auth/oauth/login", method = RequestMethod.POST)
    public LoginDto login(HttpServletResponse response,
                          @RequestBody @Valid OAuthRequest request) {
        Member member;
        if (org.apache.commons.lang3.StringUtils.isNotBlank(request.getToken())) {
            OAuthToken token = new OAuthToken();
            token.setVendor(request.getVendor());
            token.setToken(request.getToken());
            member = oauthFacade.findMember2(token);
        } else {
            member = oauthFacade.findMember(request);
        }

        if (member == null) {
            throw new BadCredentialsException("Authentication Failed. Member not exists.");
        }

        String jws = tokenHelper.generateToken(member, true);
        tokenHelper.addCookie(response, jws);

        MemberDto memberDto = new MemberDto(member);

        LoginDto loginDto = new LoginDto(true, jws, memberDto);
        loginDto.setOauthLoggedIn(true);

        return loginDto;
    }

}
