package io.scinapse.api.data.academic.repository;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.data.academic.PaperRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@XRayEnabled
public interface PaperRecommendationRepository extends JpaRepository<PaperRecommendation, PaperRecommendation.PaperRecommendationId> {
    List<PaperRecommendation> findTop5ByPaperIdOrderByScoreDesc(long paperId);
}
