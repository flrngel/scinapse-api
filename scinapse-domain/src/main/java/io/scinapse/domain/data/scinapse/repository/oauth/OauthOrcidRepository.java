package io.scinapse.domain.data.scinapse.repository.oauth;

import io.scinapse.domain.data.scinapse.model.oauth.OauthOrcid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthOrcidRepository extends JpaRepository<OauthOrcid, Long> {
    OauthOrcid findByOrcid(String orcid);
}
