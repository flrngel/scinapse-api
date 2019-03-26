package io.scinapse.domain.data.academic.repository;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.domain.data.academic.model.ConferenceInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@XRayEnabled
public interface ConferenceInstanceRepository extends JpaRepository<ConferenceInstance, Long> {
    List<ConferenceInstance> findByIdIn(List<Long> conferenceInstanceIds);
}
