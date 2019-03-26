package io.scinapse.api.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.domain.data.academic.model.Journal;
import io.scinapse.api.dto.mag.JournalDto;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.service.mag.JournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@XRayEnabled
@Component
@RequiredArgsConstructor
public class JournalFacade {

    private final JournalService journalService;

    public JournalDto find(long journalId) {
        Journal journal = journalService.find(journalId);
        if (journal == null) {
            throw new ResourceNotFoundException("Journal not found : " + journalId);
        }

        return new JournalDto(journal, true);
    }

}
