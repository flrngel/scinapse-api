package network.pluto.absolute.service.mag;

import lombok.RequiredArgsConstructor;
import network.pluto.bibliotheca.models.mag.Paper;
import network.pluto.bibliotheca.repositories.mag.PaperAuthorAffiliationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AuthorService {

    private final PaperAuthorAffiliationRepository paperAuthorAffiliationRepository;

    public List<Paper> getMainPapers(long authorId) {
        return paperAuthorAffiliationRepository.getAuthorMainPapers(authorId, new PageRequest(0, 5));
    }
}
