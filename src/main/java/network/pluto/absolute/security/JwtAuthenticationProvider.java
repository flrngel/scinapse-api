package network.pluto.absolute.security;

import io.jsonwebtoken.Claims;
import network.pluto.bibliotheca.enums.AuthorityName;
import network.pluto.bibliotheca.models.Authority;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final TokenHelper tokenHelper;

    @Autowired
    public JwtAuthenticationProvider(TokenHelper tokenHelper) {
        this.tokenHelper = tokenHelper;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String jwt = (String) authentication.getCredentials();

        Claims claims = tokenHelper.getAllClaimsFromToken(jwt);
        String email = claims.getSubject();
        List<String> roles = claims.get("role", List.class);
        List<Authority> authorities = roles.stream().map(auth -> new Authority(AuthorityName.find(auth))).collect(Collectors.toList());

        Member member = new Member();
        member.setEmail(email);
        member.setAuthorities(authorities);

        return new JwtAuthenticationToken(member);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
