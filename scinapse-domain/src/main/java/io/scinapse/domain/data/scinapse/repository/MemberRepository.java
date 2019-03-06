package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.data.scinapse.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmail(String email);
    Member findByAuthorId(long authorId);
}

