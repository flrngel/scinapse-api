package network.pluto.absolute.user;

import lombok.Data;
import network.pluto.absolute.security.Authority;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
public class Member {

    @Id
    @GeneratedValue
    private long memberId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "USER_AUTHORITY",
            joinColumns = @JoinColumn(name = "MEMBER_ID", referencedColumnName = "memberId"),
            inverseJoinColumns = @JoinColumn(name = "AUTHORITY_ID", referencedColumnName = "authorityId"))
    private List<Authority> authorities;

    @Column
    private String nickName;
}
