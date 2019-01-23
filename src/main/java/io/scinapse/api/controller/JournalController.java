package io.scinapse.api.controller;

import io.scinapse.api.dto.mag.JournalDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.facade.JournalFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
                                         PageRequest pageRequest) {
        Page<PaperDto> dtos = journalFacade.searchPaper(journalId, queryStr, pageRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("data", dtos);

        return result;
    }

}
