package io.scinapse.api.repository.author;

import java.util.List;

public interface AuthorLayerFosRepositoryCustom {
    List<Long> getRelatedFos(long authorId);
}
