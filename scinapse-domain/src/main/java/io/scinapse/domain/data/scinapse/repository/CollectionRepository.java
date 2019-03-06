package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.data.scinapse.model.Collection;
import io.scinapse.domain.data.scinapse.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRepository extends JpaRepository<Collection, Long>, CollectionRepositoryCustom {
    Page<Collection> findByCreatedByOrderByUpdatedAtDesc(Member creator, Pageable pageable);
    long countByCreatedBy(Member createdBy);
    void deleteByCreatedBy(Member createdBy);
}
