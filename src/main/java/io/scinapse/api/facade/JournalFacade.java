package io.scinapse.api.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.configuration.AcademicJpaConfig;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.data.academic.Journal;
import io.scinapse.api.dto.mag.JournalDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.enums.PaperSort;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.service.mag.JournalService;
import io.scinapse.api.util.Query;
import io.scinapse.api.util.QueryFilter;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;


@XRayEnabled
@Transactional(readOnly = true, transactionManager = AcademicJpaConfig.ACADEMIC_TX_MANAGER)
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

        return new JournalDto(journal, true);
    }

    public Page<PaperDto> getPapers(long journalId, String queryStr, PageRequest pageRequest) {
        Query query = Query.parse(queryStr);
        if (!query.isValid()) {
            throw new BadRequestException("Invalid query: too short or long query text : " + queryStr);
        }

        QueryFilter queryFilter = new QueryFilter();
        queryFilter.getJournals().add(journalId);
        query.setFilter(queryFilter);

        return paperFacade.searchFromES(query, pageRequest);
    }

    public Page<PaperDto> getDefaultPapers(long journalId, PageRequest pageRequest) {
        Query query = Query.journal(journalId);

        PaperSort sort = PaperSort.find(pageRequest.getSort());

        // apply default sorting for invalid sort param
        if (sort == null || sort == PaperSort.RELEVANCE) {
            List<SortBuilder> sorts = Arrays.asList(
                    SortBuilders.fieldSort("year").order(SortOrder.DESC),
                    SortBuilders.fieldSort("citation_count").order(SortOrder.DESC));
            return paperFacade.searchFromES(query, sorts, pageRequest);
        }

        return paperFacade.searchFromES(query, pageRequest);
    }

}
