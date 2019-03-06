package io.scinapse.api.controller;

import io.scinapse.api.dto.mag.JournalDto;
import io.scinapse.api.facade.JournalFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

}
