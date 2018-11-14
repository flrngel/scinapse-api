package io.scinapse.api.controller;

import io.scinapse.api.dto.CompletionDto;
import io.scinapse.api.dto.SuggestionDto;
import io.scinapse.api.dto.response.Response;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.service.SearchService;
import io.scinapse.api.util.TextUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SuggestController {

    private final SearchService searchService;

    @RequestMapping(value = "/complete", method = RequestMethod.GET)
    public Map<String, Object> complete(@RequestParam("q") String keyword) {
        keyword = StringUtils.normalizeSpace(keyword);

        if (StringUtils.isBlank(keyword) || keyword.length() < 2) {
            throw new BadRequestException("Keyword is too short. Only keywords with two or more characters are allowed.");
        }

        Map<String, Object> result = new HashMap<>();

        String doi = TextUtils.parseDoi(keyword);
        if (doi != null) {
            result.put("data", new ArrayList<>());
        } else {
            List<CompletionDto> dtos = searchService.completeByScholar(keyword);
            result.put("data", dtos);
        }

        return result;
    }

    @RequestMapping(value = "/complete/affiliation", method = RequestMethod.GET)
    public Response<List<CompletionDto>> completeAffiliation(@RequestParam("q") String keyword) {
        keyword = StringUtils.normalizeSpace(keyword);
        if (StringUtils.isBlank(keyword) || keyword.length() < 2) {
            throw new BadRequestException("Keyword is too short. Only keywords with two or more characters are allowed.");
        }

        return Response.success(searchService.completeAffiliation(keyword));
    }

    @RequestMapping(value = "/complete", method = RequestMethod.GET, params = "old")
    public Map<String, Object> completeOld(@RequestParam("q") String keyword) {
        keyword = StringUtils.normalizeSpace(keyword);
        if (StringUtils.isBlank(keyword) || keyword.length() < 2) {
            throw new BadRequestException("Keyword is too short. Only keywords with two or more characters are allowed.");
        }

        Map<String, Object> result = new HashMap<>();

        String doi = TextUtils.parseDoi(keyword);
        if (doi != null) {
            result.put("data", new ArrayList<>());
        } else {
            List<CompletionDto> dtos = searchService.complete(keyword);
            result.put("data", dtos);
        }

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
