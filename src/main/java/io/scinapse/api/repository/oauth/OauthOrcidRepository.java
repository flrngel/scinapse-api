package io.scinapse.api.repository.oauth;

import io.scinapse.api.model.oauth.OauthOrcid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthOrcidRepository extends JpaRepository<OauthOrcid, Long> {
    OauthOrcid findByOrcid(String orcid);
}
