package io.scinapse.api.repository.profile;

import io.scinapse.api.model.profile.ProfileFos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileFosRepository extends JpaRepository<ProfileFos, ProfileFos.ProfileFosId>, ProfileFosRepositoryCustom {
}
