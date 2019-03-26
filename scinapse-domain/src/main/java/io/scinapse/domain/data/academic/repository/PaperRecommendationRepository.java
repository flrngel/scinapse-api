package io.scinapse.domain.data.academic.repository;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.domain.data.academic.model.Paper;
import io.scinapse.domain.data.academic.model.PaperRecommendation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@XRayEnabled
public interface PaperRecommendationRepository extends JpaRepository<PaperRecommendation, PaperRecommendation.PaperRecommendationId> {
    List<PaperRecommendation> findTop5ByPaperIdOrderByScoreDesc(long paperId);

    @Query("select t.recommendedPaper from PaperRecommendation t join t.recommendedPaper WHERE t.id.paperId = :paperId order by t.recommendedPaper.citationCount desc")
    List<Paper> getHighestCitedRecommendationPapers(@Param("paperId") long paperId, Pageable pageable);
}
