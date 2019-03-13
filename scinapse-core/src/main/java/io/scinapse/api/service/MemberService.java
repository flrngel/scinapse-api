package io.scinapse.api.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.domain.data.academic.Affiliation;
import io.scinapse.domain.data.academic.repository.AffiliationRepository;
import io.scinapse.domain.data.scinapse.model.Authority;
import io.scinapse.domain.data.scinapse.model.Collection;
import io.scinapse.domain.data.scinapse.model.Member;
import io.scinapse.domain.data.scinapse.model.MemberSavedFilter;
import io.scinapse.domain.data.scinapse.repository.AuthorityRepository;
import io.scinapse.domain.data.scinapse.repository.CollectionRepository;
import io.scinapse.domain.data.scinapse.repository.MemberRepository;
import io.scinapse.domain.data.scinapse.repository.MemberSavedFilterRepository;
import io.scinapse.domain.enums.AuthorityName;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private final CollectionRepository collectionRepository;
    private final MemberSavedFilterRepository savedFilterRepository;

    @Transactional
    public Member saveMember(@NonNull Member member) {
        checkAffiliationValidity(member.getAffiliationId(), member.getAffiliationName());

        if (StringUtils.hasText(member.getPassword())) {
            member.setPassword(passwordEncoder.encode(member.getPassword()));
        }

        Authority authority = authorityRepository.findByName(AuthorityName.ROLE_UNVERIFIED);
        member.setAuthorities(Collections.singletonList(authority));

        Member save = memberRepository.save(member);

        Collection collection = new Collection();
        collection.setTitle("Read Later");
        collection.setCreatedBy(save);
        collectionRepository.save(collection);

        return save;
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

    public List<MemberSavedFilter.SavedFilter> getSavedFilters(Member member) {
        return Optional.ofNullable(savedFilterRepository.findByMemberId(member.getId()))
                .map(MemberSavedFilter::getFilter)
                .orElseGet(ArrayList::new);
    }

    @Transactional
    public List<MemberSavedFilter.SavedFilter> updateSavedFilters(Member member, List<MemberSavedFilter.SavedFilter> savedFilters) {
        MemberSavedFilter filter = new MemberSavedFilter();
        filter.setMemberId(member.getId());
        filter.setFilter(savedFilters);
        return savedFilterRepository.save(filter).getFilter();
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
