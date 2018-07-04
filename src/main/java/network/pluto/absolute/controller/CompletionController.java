package network.pluto.absolute.controller;

import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.CompletionDto;
import network.pluto.absolute.dto.SuggestionDto;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.service.SearchService;
import org.apache.commons.lang3.StringUtils;
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
        if (StringUtils.isBlank(keyword) || keyword.trim().length() < 2) {
            throw new BadRequestException("Keyword is too short. Only keywords with two or more characters are allowed.");
        }

        List<CompletionDto> dtos = searchService.complete(keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("data", dtos);

        return result;
    }

    @RequestMapping(value = "/suggest", method = RequestMethod.GET)
    public Map<String, Object> suggest(@RequestParam("q") String keyword) {
        keyword = StringUtils.strip(keyword);
        SuggestionDto suggest = searchService.suggest(keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("data", suggest);

        return result;
    }

}
