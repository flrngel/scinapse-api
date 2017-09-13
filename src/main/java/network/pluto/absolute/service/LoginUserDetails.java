package network.pluto.absolute.service;

import lombok.Getter;
import network.pluto.bibliotheca.models.Member;
import org.springframework.security.core.userdetails.User;

@Getter
public class LoginUserDetails extends User {

    private Member member;

    public LoginUserDetails(Member member) {
        super(member.getEmail(), member.getPassword(), member.getAuthorities());
        this.member = member;
    }
}
