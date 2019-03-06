package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.dto.CollectionWrapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CollectionRepositoryCustom {
    Map<Long, List<CollectionWrapper>> findBySavedPapers(long memberId, Set<Long> paperIds);
    List<CollectionRepositoryImpl.CollectionEmailDataWrapper> getCollectionEmailData();
}
