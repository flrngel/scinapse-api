package io.scinapse.domain.data.academic.repository;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.domain.data.academic.FieldsOfStudy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

@XRayEnabled
public interface FieldsOfStudyRepository extends JpaRepository<FieldsOfStudy, Long> {
    List<FieldsOfStudy> findByIdIn(Set<Long> fosIds);
}