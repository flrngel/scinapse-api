package io.scinapse.api.data.academic.repository;

import java.util.List;

public interface PaperRecommendationRepositoryCustom {
    List<Long> getReadingNow(long paperId);
}
