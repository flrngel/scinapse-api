package io.scinapse.api.data.scinapse.repository;

import io.scinapse.api.dto.v2.PaperItemDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CollectionRepositoryCustom {
    Map<Long, List<PaperItemDto.SavedInCollection>> findBySavedPapers(long memberId, Set<Long> paperIds);
}
