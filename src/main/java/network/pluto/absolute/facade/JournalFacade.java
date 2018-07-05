package network.pluto.absolute.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.JournalDto;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.enums.PaperSort;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.model.mag.Journal;
import network.pluto.absolute.service.mag.JournalService;
import network.pluto.absolute.util.Query;
import network.pluto.absolute.util.QueryFilter;
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
