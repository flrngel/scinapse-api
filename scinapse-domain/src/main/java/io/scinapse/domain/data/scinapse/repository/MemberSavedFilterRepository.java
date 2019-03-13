package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.data.scinapse.model.MemberSavedFilter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberSavedFilterRepository extends JpaRepository<MemberSavedFilter, Long> {
    MemberSavedFilter findByMemberId(long memberId);
}
