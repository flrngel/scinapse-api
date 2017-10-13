package network.pluto.absolute.security.jwt;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
public class JwtUser extends AbstractAuthenticationToken {

    private long id;
    private String email;
    private String name;
    private List<GrantedAuthority> authorities;

    public JwtUser(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this;
    }
}
