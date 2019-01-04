package io.scinapse.api.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.data.academic.Affiliation;
import io.scinapse.api.data.academic.repository.AffiliationRepository;
import io.scinapse.api.data.scinapse.model.Authority;
import io.scinapse.api.data.scinapse.model.Member;
import io.scinapse.api.data.scinapse.repository.AuthorityRepository;
import io.scinapse.api.data.scinapse.repository.MemberRepository;
import io.scinapse.api.enums.AuthorityName;
import io.scinapse.api.error.BadRequestException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Optional;

@XRayEnabled
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final AffiliationRepository affiliationRepository;

    @Transactional
    public Member saveMember(@NonNull Member member) {
        checkAffiliationValidity(member.getAffiliationId(), member.getAffiliationName());

        if (StringUtils.hasText(member.getPassword())) {
            member.setPassword(passwordEncoder.encode(member.getPassword()));
        }

        Authority authority = authorityRepository.findByName(AuthorityName.ROLE_UNVERIFIED);
        member.setAuthorities(Collections.singletonList(authority));

        return memberRepository.save(member);
    }

    @Transactional
    public Member updateMember(@NonNull Member old, @NonNull Member updated) {
        checkAffiliationValidity(updated.getAffiliationId(), updated.getAffiliationName());

        old.setFirstName(updated.getFirstName());
        old.setLastName(updated.getLastName());
        old.setAffiliationId(updated.getAffiliationId());
        old.setAffiliationName(updated.getAffiliationName());
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

    private void checkAffiliationValidity(Long affiliationId, String affiliationName) {
        if (org.apache.commons.lang3.StringUtils.isBlank(affiliationName)) {
            throw new BadRequestException("Affiliation name must exist.");
        }

        if (affiliationId == null) {
            // custom affiliation. no need to check.
            return;
        }

        // user tries to use auto completed affiliation. check if user modified affiliation name.
        Affiliation updatedAffiliation = Optional.ofNullable(affiliationRepository.findOne(affiliationId))
                .orElseThrow(() -> new BadRequestException("Cannot update affiliation with invalid affiliation Id: " + affiliationId));
        if (!org.apache.commons.lang3.StringUtils.equals(updatedAffiliation.getName(), affiliationName)) {
            throw new BadRequestException("The affiliation name was modified. " +
                    "Original Id: [ " + updatedAffiliation.getId() + " ], " +
                    "Original name: [ " + updatedAffiliation.getName() + " ], " +
                    "Updated name: [ " + affiliationName + " ]");
        }
    }

}
