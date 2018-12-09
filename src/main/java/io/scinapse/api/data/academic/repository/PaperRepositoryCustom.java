package io.scinapse.api.data.academic.repository;

import java.util.List;
import java.util.Set;

public interface PaperRepositoryCustom {
    List<Long> calculateFos(Set<Long> paperIds);
    List<Long> calculateCoauthor(long authorId, Set<Long> paperIds);
}
