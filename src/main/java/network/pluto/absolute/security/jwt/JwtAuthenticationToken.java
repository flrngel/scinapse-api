package network.pluto.absolute.security.jwt;

import lombok.Getter;
import network.pluto.bibliotheca.models.Member;
import org.springframework.security.authentication.AbstractAuthenticationToken;

@Getter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private String token;
    private Member member;

    public JwtAuthenticationToken(String token) {
        super(null);
        this.token = token;
        setAuthenticated(false);
    }

    public JwtAuthenticationToken(String token, Member member) {
        super(member.getAuthorities());
        this.token = token;
        this.member = member;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return member;
    }
}
