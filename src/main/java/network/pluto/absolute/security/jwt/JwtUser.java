package network.pluto.absolute.security.jwt;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@Data
public class JwtUser {

    private long id;
    private String email;
    private String name;
    private List<GrantedAuthority> authorities;
}
