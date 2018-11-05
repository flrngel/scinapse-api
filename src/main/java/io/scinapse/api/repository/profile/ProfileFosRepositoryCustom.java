package io.scinapse.api.repository.profile;

import java.util.List;

public interface ProfileFosRepositoryCustom {
    List<Long> getRelatedFos(List<Long> authorIds);
}
