package io.scinapse.domain.data.academic.repository;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.domain.data.academic.model.ConferenceSeries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@XRayEnabled
public interface ConferenceSeriesRepository extends JpaRepository<ConferenceSeries, Long> {
    List<ConferenceSeries> findByIdIn(List<Long> conferenceSeriesIds);
}
