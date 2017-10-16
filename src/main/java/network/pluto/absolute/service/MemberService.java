package network.pluto.absolute.service;

import lombok.NonNull;
import network.pluto.bibliotheca.enums.AuthorityName;
import network.pluto.bibliotheca.models.Authority;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.MemberReputation;
import network.pluto.bibliotheca.repositories.AuthorityRepository;
import network.pluto.bibliotheca.repositories.MemberRepository;
import network.pluto.bibliotheca.repositories.MemberReputationRepository;
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
    private final MemberReputationRepository memberReputationRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository,
                         MemberReputationRepository memberReputationRepository,
                         AuthorityRepository authorityRepository,
                         PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.memberReputationRepository = memberReputationRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Member saveMember(@NonNull Member member) {
        member.setPassword(passwordEncoder.encode(member.getPassword()));

        Authority authority = authorityRepository.findByName(AuthorityName.ROLE_USER);
        member.setAuthorities(Collections.singletonList(authority));

        return memberRepository.save(member);
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member findMember(long id) {
        return memberRepository.findOne(id);
    }

    public Member getMember(long id) {
        return memberRepository.getOne(id);
    }

    public Member findByEmail(String username) {
        return memberRepository.findByEmail(username);
    }

    public Member getByEmail(String email, boolean initAuthority) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            return null;
        }

        if (initAuthority) {
            member.getAuthorities().iterator();
        }
        return member;
    }

    public Member increaseReputation(Member member, int point) {
        MemberReputation reputation = new MemberReputation();
        reputation.setMember(member);
        memberReputationRepository.save(reputation);

        member.setReputation(member.getReputation() + point);
        return member;
    }
}
