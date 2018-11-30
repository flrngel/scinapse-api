package io.scinapse.api.data.scinapse.repository.oauth;

import io.scinapse.api.data.scinapse.model.oauth.OauthGoogle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthGoogleRepository extends JpaRepository<OauthGoogle, Long> {
    OauthGoogle findByGoogleId(String googleId);
}
