package network.pluto.absolute.security.jwt;

import io.jsonwebtoken.Claims;
import network.pluto.absolute.security.TokenHelper;
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
        List<String> roles = claims.get("roles", List.class);
        List<Authority> authorities = roles.stream().map(auth -> new Authority(AuthorityName.find(auth))).collect(Collectors.toList());
        String fullName = claims.get("name", String.class);

        Member member = new Member();
        member.setEmail(claims.getSubject());
        member.setAuthorities(authorities);
        member.setFullName(fullName);

        return new JwtAuthenticationToken(jwt, member);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
