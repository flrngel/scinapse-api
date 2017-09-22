package network.pluto.absolute.security.jwt;

import lombok.Getter;
import network.pluto.bibliotheca.models.Member;
import org.springframework.security.authentication.AbstractAuthenticationToken;

@Getter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private String token;
    private Member principle;

    public JwtAuthenticationToken(String token) {
        super(null);
        this.token = token;
        setAuthenticated(false);
    }

    public JwtAuthenticationToken(Member principle) {
        super(principle.getAuthorities());
        this.principle = principle;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return principle;
    }
}
