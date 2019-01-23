package io.scinapse.api.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.configuration.AcademicJpaConfig;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.data.academic.Journal;
import io.scinapse.api.dto.mag.JournalDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.service.SearchService;
import io.scinapse.api.service.mag.JournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@XRayEnabled
@Transactional(readOnly = true, transactionManager = AcademicJpaConfig.ACADEMIC_TX_MANAGER)
@Component
@RequiredArgsConstructor
public class JournalFacade {

    private final JournalService journalService;
    private final PaperFacade paperFacade;
    private final SearchService searchService;


    public JournalDto find(long journalId) {
        Journal journal = journalService.find(journalId);
        if (journal == null) {
            throw new ResourceNotFoundException("Journal not found : " + journalId);
        }

        return new JournalDto(journal, true);
    }

    public Page<PaperDto> searchPaper(long journalId, String queryStr, PageRequest pageRequest) {
        Page<Long> paperIdPage = searchService.searchJournalPaper(journalId, queryStr, pageRequest);
        List<PaperDto> dtos = paperFacade.findIn(paperIdPage.getContent());
        return new PageImpl<>(dtos, pageRequest.toPageable(), paperIdPage.getTotalElements());
    }

}
