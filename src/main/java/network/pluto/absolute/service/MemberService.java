package network.pluto.absolute.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import network.pluto.bibliotheca.enums.AuthorityName;
import network.pluto.bibliotheca.models.Authority;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.repositories.AuthorityRepository;
import network.pluto.bibliotheca.repositories.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Member saveMember(@NonNull Member member) {
        if (StringUtils.hasText(member.getPassword())) {
            member.setPassword(passwordEncoder.encode(member.getPassword()));
        }

        Authority authority = authorityRepository.findByName(AuthorityName.ROLE_UNVERIFIED);
        member.setAuthorities(Collections.singletonList(authority));

        return memberRepository.save(member);
    }

    @Transactional
    public Member updateMember(@NonNull Member old, @NonNull Member updated) {
        old.setName(updated.getName());
        old.setAffiliation(updated.getAffiliation());
        old.setMajor(updated.getMajor());
        return old;
    }

    @Transactional
    public void updatePassword(@NonNull Member old, @NonNull String password) {
        old.setPassword(passwordEncoder.encode(password));
    }

    @Transactional
    public void updateAuthority(Member member, AuthorityName name) {
        Authority authority = authorityRepository.findByName(name);
        member.setAuthorities(Collections.singletonList(authority));
    }

    public Page<Member> findAll(Pageable pageable) {
        return memberRepository.findAll(pageable);
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

}
