package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.data.scinapse.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    Member findByEmail(String email);
    Member findByAuthorId(long authorId);
}

