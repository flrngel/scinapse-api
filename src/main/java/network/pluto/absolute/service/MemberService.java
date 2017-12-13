package network.pluto.absolute.service;

import lombok.NonNull;
import network.pluto.bibliotheca.enums.AuthorityName;
import network.pluto.bibliotheca.enums.ReputationChangeReason;
import network.pluto.bibliotheca.models.Authority;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.MemberReputation;
import network.pluto.bibliotheca.repositories.AuthorityRepository;
import network.pluto.bibliotheca.repositories.MemberRepository;
import network.pluto.bibliotheca.repositories.MemberReputationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Transactional(readOnly = true)
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
        old.setProfileImage(updated.getProfileImage());
        old.setInstitution(updated.getInstitution());
        old.setMajor(updated.getMajor());
        return old;
    }

    @Transactional
    public void updatePassword(@NonNull Member old, @NonNull String password) {
        old.setPassword(passwordEncoder.encode(password));
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

    @Transactional
    public void changeReputation(Member member, ReputationChangeReason reason, long point) {
        MemberReputation reputation = new MemberReputation();
        reputation.setMember(member);
        reputation.setReason(reason);
        reputation.setDelta(point);
        memberReputationRepository.save(reputation);

        member.changeReputation(point);
    }
}
