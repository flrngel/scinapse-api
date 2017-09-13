package network.pluto.absolute.service;

import network.pluto.bibliotheca.enums.AuthorityName;
import network.pluto.bibliotheca.models.Authority;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.repositories.AuthorityRepository;
import network.pluto.bibliotheca.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Member save(Member member) {
        member.setPassword(passwordEncoder.encode(member.getPassword()));

        Authority authority = authorityRepository.findByName(AuthorityName.ROLE_USER);
        member.setAuthorities(Collections.singletonList(authority));

        return memberRepository.save(member);
    }

    public List<Member> getAll() {
        return memberRepository.findAll();
    }

    public Member getMember(long id) {
        return memberRepository.findOne(id);
    }

    public Member findByEmail(String username) {
        return memberRepository.findByEmail(username);
    }
}
