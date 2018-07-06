package io.scinapse.api.service.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.model.mag.Journal;
import io.scinapse.api.repository.mag.JournalRepository;
import io.scinapse.api.service.SearchService;
import lombok.RequiredArgsConstructor;
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
