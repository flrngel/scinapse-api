package io.scinapse.api.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.enums.AuthorityName;
import io.scinapse.api.model.Authority;
import io.scinapse.api.model.Member;
import io.scinapse.api.repository.AuthorityRepository;
import io.scinapse.api.repository.MemberRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;

@XRayEnabled
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
        old.setFirstName(updated.getFirstName());
        old.setLastName(updated.getLastName());
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
