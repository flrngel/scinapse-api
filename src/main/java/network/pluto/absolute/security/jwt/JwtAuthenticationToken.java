package network.pluto.absolute.security.jwt;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

@Getter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private String token;
    private JwtUser jwtUser;

    public JwtAuthenticationToken(String token, JwtUser jwtUser) {
        super(jwtUser.getAuthorities());
        this.token = token;
        this.jwtUser = jwtUser;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return jwtUser;
    }
}
