package network.pluto.absolute.user;

import lombok.Getter;
import network.pluto.bibliotheca.models.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.Collection;

@Getter
public class UserDetailsImpl extends User {

    private String nickName;

    public UserDetailsImpl(Member member) {
        super(member.getEmail(), member.getPassword(), member.getAuthorities());
        this.nickName = member.getNickName();
    }
}
