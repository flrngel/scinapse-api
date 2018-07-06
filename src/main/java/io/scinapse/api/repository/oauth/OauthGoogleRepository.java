package io.scinapse.api.repository.oauth;

import io.scinapse.api.model.oauth.OauthGoogle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthGoogleRepository extends JpaRepository<OauthGoogle, Long> {
    OauthGoogle findByGoogleId(String googleId);
}
