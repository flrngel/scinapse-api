package network.pluto.absolute.security.rest;

import network.pluto.absolute.model.LoginUserDetails;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class RestAuthenticationProvider implements AuthenticationProvider {

    private final MemberService memberService;
    private final BCryptPasswordEncoder encoder;

    @Autowired
    public RestAuthenticationProvider(MemberService memberService, BCryptPasswordEncoder encoder) {
        this.memberService = memberService;
        this.encoder = encoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.notNull(authentication, "No authentication data provided");

        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        Member member = memberService.findByEmail(username);
        if (member == null) {
            throw new UsernameNotFoundException("Member not found: " + username);
        }

        if (!encoder.matches(password, member.getPassword())) {
            throw new BadCredentialsException("Authentication Failed. Username or Password not valid.");
        }

        if (member.getAuthorities() == null) {
            throw new InsufficientAuthenticationException("Member has no roles assigned");
        }

        return new UsernamePasswordAuthenticationToken(new LoginUserDetails(member), null, member.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
