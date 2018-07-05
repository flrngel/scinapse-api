package network.pluto.absolute.repositories.oauth;

import network.pluto.absolute.models.oauth.OauthFacebook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthFacebookRepository extends JpaRepository<OauthFacebook, Long> {
    OauthFacebook findByFacebookId(String facebookId);
}
