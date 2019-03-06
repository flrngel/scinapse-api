package io.scinapse.domain.data.academic.repository;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.domain.data.academic.Journal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@XRayEnabled
public interface JournalRepository extends JpaRepository<Journal, Long> {
    List<Journal> findByIdIn(List<Long> journalIds);
}
