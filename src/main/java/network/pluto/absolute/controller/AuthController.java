package network.pluto.absolute.controller;

import com.google.common.base.Strings;
import network.pluto.absolute.dto.LoginDto;
import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.dto.OAuthAuthorizeUriDto;
import network.pluto.absolute.dto.OrcidDto;
import network.pluto.absolute.enums.OAuthVendor;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.facade.MemberFacade;
import network.pluto.absolute.facade.OAuthOrcidFacade;
import network.pluto.absolute.security.TokenHelper;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.Orcid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AuthController {

    private final TokenHelper tokenHelper;
    private final MemberService memberService;
    private final OAuthOrcidFacade oAuthOrcidFacade;
    private final MemberFacade memberFacade;

    @Autowired
    public AuthController(TokenHelper tokenHelper,
                          MemberService memberService,
                          OAuthOrcidFacade oAuthOrcidFacade,
                          MemberFacade memberFacade) {
        this.tokenHelper = tokenHelper;
        this.memberService = memberService;
        this.oAuthOrcidFacade = oAuthOrcidFacade;
        this.memberFacade = memberFacade;
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

    @RequestMapping(value = "/auth/oauth/authorize-uri", method = RequestMethod.GET)
    public OAuthAuthorizeUriDto getAuthorizeUri(@RequestParam OAuthVendor vendor) {
        if (vendor != OAuthVendor.ORCID) {
            throw new BadRequestException("Vendor not supported: " + vendor);
        }

        URI uri = oAuthOrcidFacade.getAuthorizeUri();

        OAuthAuthorizeUriDto dto = new OAuthAuthorizeUriDto();
        dto.setVendor(vendor);
        dto.setUri(uri);

        return dto;
    }

    @RequestMapping(value = "/auth/oauth/authenticate", method = RequestMethod.POST)
    public MemberDto authenticate(@ApiIgnore JwtUser user,
                                  @RequestParam OAuthVendor vendor,
                                  @RequestParam String code) {
        if (vendor != OAuthVendor.ORCID) {
            throw new BadRequestException("Vendor not supported: " + vendor);
        }

        Orcid orcid = oAuthOrcidFacade.exchange(code);
        return memberFacade.authenticate(user.getId(), orcid);
    }

    @RequestMapping(value = "/auth/oauth/exchange", method = RequestMethod.POST)
    public OrcidDto exchange(@RequestParam OAuthVendor vendor,
                             @RequestParam String code) {
        if (vendor != OAuthVendor.ORCID) {
            throw new BadRequestException("Vendor not supported: " + vendor);
        }

        return new OrcidDto(oAuthOrcidFacade.exchange(code), true);
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
