package io.scinapse.api.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.dto.JournalDto;
import io.scinapse.api.dto.PaperDto;
import io.scinapse.api.enums.PaperSort;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.model.mag.Journal;
import io.scinapse.api.service.mag.JournalService;
import io.scinapse.api.util.Query;
import io.scinapse.api.util.QueryFilter;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;


@XRayEnabled
@Transactional(readOnly = true)
@Component
@RequiredArgsConstructor
public class JournalFacade {

    private final JournalService journalService;
    private final PaperFacade paperFacade;

    public JournalDto find(long journalId) {
        Journal journal = journalService.find(journalId);
        if (journal == null) {
            throw new ResourceNotFoundException("Journal not found : " + journalId);
        }

        return new JournalDto(journal);
    }

    public Page<PaperDto> getPapers(long journalId, String queryStr, Pageable pageable) {
        Query query = Query.parse(queryStr);
        if (!query.isValid()) {
            throw new BadRequestException("Invalid query: too short or long query text : " + queryStr);
        }

        QueryFilter queryFilter = new QueryFilter();
        queryFilter.getJournals().add(journalId);
        query.setFilter(queryFilter);

        return paperFacade.searchFromES(query, pageable);
    }

    public Page<PaperDto> getDefaultPapers(long journalId, Pageable pageable) {
        Query query = Query.parse(null);
        query.setJournalId(journalId);
        query.setJournalSearch(true);

        SortBuilder sortBuilder = PaperSort.toSortBuilder(PaperSort.NEWEST_FIRST);
        return paperFacade.searchFromES(query, Collections.singletonList(sortBuilder), pageable);
    }

}
