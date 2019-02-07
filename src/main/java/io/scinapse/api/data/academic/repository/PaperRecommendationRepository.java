package io.scinapse.api.data.academic.repository;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.data.academic.Paper;
import io.scinapse.api.data.academic.PaperRecommendation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@XRayEnabled
public interface PaperRecommendationRepository extends JpaRepository<PaperRecommendation, PaperRecommendation.PaperRecommendationId>, PaperRecommendationRepositoryCustom {
    List<PaperRecommendation> findTop5ByPaperIdOrderByScoreDesc(long paperId);

    @Query("select t.recommendedPaper from PaperRecommendation t join t.recommendedPaper WHERE t.id.paperId = :paperId order by t.recommendedPaper.citationCount desc")
    List<Paper> getHighestCitedRecommendationPapers(@Param("paperId") long paperId, Pageable pageable);
}
