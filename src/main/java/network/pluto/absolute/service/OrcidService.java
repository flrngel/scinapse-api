package network.pluto.absolute.service;

import network.pluto.bibliotheca.models.Orcid;
import network.pluto.bibliotheca.repositories.OrcidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

@Component
public class OrcidService {

    private final OrcidRepository orcidRepository;

    @Autowired
    public OrcidService(OrcidRepository orcidRepository) {
        this.orcidRepository = orcidRepository;
    }

    @Transactional
    public Orcid create(Orcid orcid) {
        return orcidRepository.save(orcid);
    }

    public Orcid findByOrcid(String orcid) {
        return orcidRepository.findByOrcid(orcid);
    }

    @Transactional
    public Orcid update(@Nonnull Orcid old, @Nonnull Orcid updated) {
        old.setOrcid(updated.getOrcid());
        old.setName(updated.getName());
        old.setAccessToken(updated.getAccessToken());
        return old;
    }
}
