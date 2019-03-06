package io.scinapse.api.service.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.domain.configuration.AcademicJpaConfig;
import io.scinapse.domain.data.academic.Journal;
import io.scinapse.domain.data.academic.repository.JournalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@XRayEnabled
@Transactional(readOnly = true, transactionManager = AcademicJpaConfig.ACADEMIC_TX_MANAGER)
@Service
@RequiredArgsConstructor
public class JournalService {

    private final JournalRepository repository;

    public Journal find(long journalId) {
        return repository.findOne(journalId);
    }

}
