package io.scinapse.domain.data.academic.repository;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface PaperRepositoryCustom {
    List<Long> calculateFos(Set<Long> paperIds);
    List<Long> calculateCoauthor(long authorId, Set<Long> paperIds);
    List<Long> extractTopRefPapers(Set<Long> paperIds, Pageable pageable);
}
