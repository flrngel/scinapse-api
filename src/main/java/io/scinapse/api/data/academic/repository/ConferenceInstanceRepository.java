package io.scinapse.api.data.academic.repository;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.data.academic.ConferenceInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@XRayEnabled
public interface ConferenceInstanceRepository extends JpaRepository<ConferenceInstance, Long> {
    List<ConferenceInstance> findByIdIn(List<Long> conferenceInstanceIds);
}
