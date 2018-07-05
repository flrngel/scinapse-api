package network.pluto.absolute.repository;

import network.pluto.absolute.model.Collection;
import network.pluto.absolute.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    Page<Collection> findByCreatedByOrderByCreatedAtDesc(Member creator, Pageable pageable);
    long countByCreatedBy(Member creator);
}
