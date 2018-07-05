package network.pluto.absolute.repository.oauth;

import network.pluto.absolute.model.oauth.OauthGoogle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthGoogleRepository extends JpaRepository<OauthGoogle, Long> {
    OauthGoogle findByGoogleId(String googleId);
}
