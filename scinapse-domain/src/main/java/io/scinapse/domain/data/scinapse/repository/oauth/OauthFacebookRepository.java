package io.scinapse.domain.data.scinapse.repository.oauth;

import io.scinapse.domain.data.scinapse.model.oauth.OauthFacebook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthFacebookRepository extends JpaRepository<OauthFacebook, Long> {
    OauthFacebook findByFacebookId(String facebookId);
}
