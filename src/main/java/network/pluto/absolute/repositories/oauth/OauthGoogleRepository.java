package network.pluto.absolute.repositories.oauth;

import network.pluto.absolute.models.oauth.OauthGoogle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthGoogleRepository extends JpaRepository<OauthGoogle, Long> {
    OauthGoogle findByGoogleId(String googleId);
}
