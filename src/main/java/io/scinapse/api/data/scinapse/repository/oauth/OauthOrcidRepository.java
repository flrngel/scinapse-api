package io.scinapse.api.data.scinapse.repository.oauth;

import io.scinapse.api.data.scinapse.model.oauth.OauthOrcid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthOrcidRepository extends JpaRepository<OauthOrcid, Long> {
    OauthOrcid findByOrcid(String orcid);
}
