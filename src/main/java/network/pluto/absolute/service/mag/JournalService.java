package network.pluto.absolute.service.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.RequiredArgsConstructor;
import network.pluto.absolute.models.mag.Journal;
import network.pluto.absolute.repositories.mag.JournalRepository;
import network.pluto.absolute.service.SearchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@XRayEnabled
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class JournalService {

    private final JournalRepository repository;
    private final SearchService searchService;

    public Journal find(long journalId) {
        return repository.findOne(journalId);
    }

    public void searchPapers(String text) {
    }

}
