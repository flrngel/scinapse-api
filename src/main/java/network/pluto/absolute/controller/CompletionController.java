package network.pluto.absolute.controller;

import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.CompletionDto;
import network.pluto.absolute.dto.SuggestionDto;
import network.pluto.absolute.service.SearchService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CompletionController {

    private final SearchService searchService;

    @RequestMapping(value = "/complete", method = RequestMethod.GET)
    public Map<String, Object> complete(@RequestParam("q") String keyword) {
        List<CompletionDto> dtos = searchService.complete(keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("data", dtos);

        return result;
    }

    @RequestMapping(value = "/suggest", method = RequestMethod.GET)
    public Map<String, Object> suggest(@RequestParam("q") String keyword) {
        SuggestionDto suggest = searchService.suggest(keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("data", suggest);

        return result;
    }

}
