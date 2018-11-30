package io.scinapse.api.data.academic.repository;

import java.util.List;

public interface AuthorRepositoryCustom {
    List<Long> getRelatedFos(long authorId);
}
