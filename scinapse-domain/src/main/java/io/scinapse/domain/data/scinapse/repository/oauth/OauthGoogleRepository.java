package io.scinapse.domain.data.scinapse.repository.oauth;

import io.scinapse.domain.data.scinapse.model.oauth.OauthGoogle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthGoogleRepository extends JpaRepository<OauthGoogle, Long> {
    OauthGoogle findByGoogleId(String googleId);
}
