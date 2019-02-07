package io.scinapse.api.data.academic.repository;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.data.academic.ConferenceSeries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@XRayEnabled
public interface ConferenceSeriesRepository extends JpaRepository<ConferenceSeries, Long> {
    List<ConferenceSeries> findByIdIn(List<Long> conferenceSeriesIds);
}
