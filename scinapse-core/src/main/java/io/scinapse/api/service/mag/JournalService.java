package io.scinapse.api.service.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.domain.data.academic.model.Journal;
import io.scinapse.domain.data.academic.repository.JournalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@XRayEnabled
@Service
@RequiredArgsConstructor
public class JournalService {

    private final JournalRepository repository;

    public Journal find(long journalId) {
        return repository.findOne(journalId);
    }

}
