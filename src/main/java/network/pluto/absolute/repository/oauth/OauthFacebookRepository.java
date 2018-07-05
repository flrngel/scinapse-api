package network.pluto.absolute.repository.oauth;

import network.pluto.absolute.model.oauth.OauthFacebook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthFacebookRepository extends JpaRepository<OauthFacebook, Long> {
    OauthFacebook findByFacebookId(String facebookId);
}
