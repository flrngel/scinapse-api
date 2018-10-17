package io.scinapse.api.repository.profile;

import io.scinapse.api.model.profile.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, String>, ProfileRepositoryCustom {
}
