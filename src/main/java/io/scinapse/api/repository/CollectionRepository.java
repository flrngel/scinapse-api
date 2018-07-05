package io.scinapse.api.repository;

import io.scinapse.api.model.Collection;
import io.scinapse.api.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    Page<Collection> findByCreatedByOrderByUpdatedAtDesc(Member creator, Pageable pageable);
    long countByCreatedBy(Member creator);
}
