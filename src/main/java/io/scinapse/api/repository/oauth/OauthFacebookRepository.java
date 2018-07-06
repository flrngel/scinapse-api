package io.scinapse.api.repository.oauth;

import io.scinapse.api.model.oauth.OauthFacebook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthFacebookRepository extends JpaRepository<OauthFacebook, Long> {
    OauthFacebook findByFacebookId(String facebookId);
}
