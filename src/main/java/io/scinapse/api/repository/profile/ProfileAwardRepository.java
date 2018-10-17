package io.scinapse.api.repository.profile;

import io.scinapse.api.model.profile.ProfileAward;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileAwardRepository extends JpaRepository<ProfileAward, String> {
}
