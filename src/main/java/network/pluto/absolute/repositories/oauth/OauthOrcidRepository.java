package network.pluto.absolute.repositories.oauth;

import network.pluto.absolute.models.oauth.OauthOrcid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthOrcidRepository extends JpaRepository<OauthOrcid, Long> {
    OauthOrcid findByOrcid(String orcid);
}
