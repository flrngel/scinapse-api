package io.scinapse.api.repository.profile;

import io.scinapse.api.model.mag.Author;
import io.scinapse.api.model.profile.Profile;
import io.scinapse.api.model.profile.ProfileAuthor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileAuthorRepository extends JpaRepository<ProfileAuthor, ProfileAuthor.ProfileAuthorId> {
    List<ProfileAuthor> findByIdAuthorIdIn(List<Long> authorIds);
    List<ProfileAuthor> findByIdProfileId(String profileId);
}
