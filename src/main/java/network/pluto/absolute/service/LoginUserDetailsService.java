package network.pluto.absolute.service;

import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LoginUserDetailsService implements UserDetailsService {

    private final MemberService memberService;

    @Autowired
    public LoginUserDetailsService(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberService.findByEmail(username);
        if (member == null) {
            throw new UsernameNotFoundException("The requested user is not found: " + username);
        }
        return new LoginUserDetails(member);
    }
}
