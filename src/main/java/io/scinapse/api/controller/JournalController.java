package io.scinapse.api.controller;

import io.scinapse.api.dto.JournalDto;
import io.scinapse.api.dto.PaperDto;
import io.scinapse.api.facade.JournalFacade;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JournalController {

    private final JournalFacade journalFacade;

    @RequestMapping(value = "/journals/{journalId}", method = RequestMethod.GET)
    public Map<String, Object> getJournal(@PathVariable long journalId) {
        JournalDto dto = journalFacade.find(journalId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", dto);

        return result;
    }

    @RequestMapping(value = "/journals/{journalId}/papers", method = RequestMethod.GET)
    public Map<String, Object> getPapers(@PathVariable long journalId,
                                         @RequestParam(value = "query", required = false) String queryStr,
                                         @PageableDefault Pageable pageable) {
        Page<PaperDto> dtos;
        if (StringUtils.isBlank(queryStr)) {
            dtos = journalFacade.getDefaultPapers(journalId, pageable);
        } else {
            dtos = journalFacade.getPapers(journalId, queryStr, pageable);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", dtos);

        return result;
    }

}
