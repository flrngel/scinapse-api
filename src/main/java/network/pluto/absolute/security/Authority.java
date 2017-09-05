package network.pluto.absolute.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

@Data
@Entity
public class Authority implements GrantedAuthority {

    @Id
    @GeneratedValue
    private long authorityId;

    @JsonIgnore
    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private AuthorityName name;

    @Override
    public String getAuthority() {
        return name.name();
    }
}
