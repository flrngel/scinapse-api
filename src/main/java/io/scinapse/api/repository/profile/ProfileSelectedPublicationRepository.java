package io.scinapse.api.repository.profile;

import io.scinapse.api.model.profile.ProfileSelectedPublication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileSelectedPublicationRepository extends JpaRepository<ProfileSelectedPublication, ProfileSelectedPublication.ProfileSelectedPublicationId> {
    void deleteByProfileId(String profileId);
    List<ProfileSelectedPublication> findByIdProfileId(String profileId);
}
