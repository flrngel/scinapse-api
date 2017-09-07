package network.pluto.absolute.user;

import network.pluto.absolute.security.Authority;
import network.pluto.absolute.security.AuthorityName;
import network.pluto.absolute.security.AuthorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
public class MemberController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping(value = "/members", method = RequestMethod.POST)
    public Member create(@RequestBody Member member) {
        member.setPassword(passwordEncoder.encode(member.getPassword()));

        Authority authority = authorityRepository.findByName(AuthorityName.ROLE_USER);
        member.setAuthorities(Collections.singletonList(authority));

        return memberRepository.save(member);
    }

    @RequestMapping(value = "/members", method = RequestMethod.GET)
    public List<Member> readMembers() {
        return memberRepository.findAll();
    }

    @RequestMapping(value = "/members/{id}", method = RequestMethod.GET)
    public Member readMember(@PathVariable long id) {
        return memberRepository.findOne(id);
    }
}
