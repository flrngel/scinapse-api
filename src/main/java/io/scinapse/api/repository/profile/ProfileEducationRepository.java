package io.scinapse.api.repository.profile;

import io.scinapse.api.model.profile.ProfileEducation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileEducationRepository extends JpaRepository<ProfileEducation, String> {
}
