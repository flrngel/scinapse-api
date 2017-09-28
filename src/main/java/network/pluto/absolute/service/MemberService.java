package network.pluto.absolute.service;

import network.pluto.bibliotheca.enums.AuthorityName;
import network.pluto.bibliotheca.models.Authority;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.repositories.AuthorityRepository;
import network.pluto.bibliotheca.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

@Transactional
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository,
                         AuthorityRepository authorityRepository,
                         PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
    }

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

    public Member getByEmail(String email, boolean initAuthority) {
        Member member = memberRepository.findByEmail(email);
        if (initAuthority) {
            member.getAuthorities().iterator();
        }
        return member;
    }
}
