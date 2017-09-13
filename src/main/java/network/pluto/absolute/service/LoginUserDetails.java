package network.pluto.absolute.service;

import lombok.Getter;
import network.pluto.bibliotheca.models.Member;
import org.springframework.security.core.userdetails.User;

@Getter
public class LoginUserDetails extends User {

    private String fullName;

    public LoginUserDetails(Member member) {
        super(member.getEmail(), member.getPassword(), member.getAuthorities());
        this.fullName = member.getFullName();
    }
}
