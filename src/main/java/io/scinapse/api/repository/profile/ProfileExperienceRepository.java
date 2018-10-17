package io.scinapse.api.repository.profile;

import io.scinapse.api.model.profile.ProfileExperience;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileExperienceRepository extends JpaRepository<ProfileExperience, String> {
}
