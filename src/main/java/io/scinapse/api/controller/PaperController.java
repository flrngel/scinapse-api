package io.scinapse.api.controller;

import io.scinapse.api.dto.AggregationDto;
import io.scinapse.api.dto.CitationTextDto;
import io.scinapse.api.dto.PaperDto;
import io.scinapse.api.enums.CitationFormat;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.facade.PaperFacade;
import io.scinapse.api.util.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PaperController {

    private final PaperFacade paperFacade;

    @RequestMapping(value = "/papers/{paperId}", method = RequestMethod.GET)
    public PaperDto find(@PathVariable long paperId) {
        return paperFacade.find(paperId);
    }

    @RequestMapping(value = "/papers", method = RequestMethod.GET)
    public Page<PaperDto> search(@RequestParam("query") String queryStr,
                                 @RequestParam(value = "filter", required = false) String filterStr,
                                 @PageableDefault Pageable pageable) {
        Query query = Query.parse(queryStr, filterStr);
        if (!query.isValid()) {
            throw new BadRequestException("Invalid query: too short or long query text");
        }

        return paperFacade.search(query, pageable);
    }

    @RequestMapping(value = "/papers/aggregate", method = RequestMethod.GET)
    public Map<String, Object> aggregate(@RequestParam("query") String queryStr,
                                         @RequestParam(value = "filter", required = false) String filterStr) {
        Query query = Query.parse(queryStr, filterStr);
        if (!query.isValid()) {
            throw new BadRequestException("Invalid query: too short or long query text");
        }

        HashMap<String, Object> result = new HashMap<>();

        AggregationDto aggregationDto = paperFacade.aggregate(query);
        result.put("data", aggregationDto);

        Meta meta = new Meta();
        meta.available = aggregationDto.available;
        result.put("meta", meta);

        return result;
    }

    @RequestMapping(value = "/papers/{paperId}/references", method = RequestMethod.GET)
    public Page<PaperDto> paperReferences(@PathVariable long paperId,
                                          @PageableDefault Pageable pageable) {
        return paperFacade.findReferences(paperId, pageable);
    }

    @RequestMapping(value = "/papers/{paperId}/cited", method = RequestMethod.GET)
    public Page<PaperDto> paperCited(@PathVariable long paperId,
                                     @PageableDefault Pageable pageable) {
        return paperFacade.findCited(paperId, pageable);
    }

    @RequestMapping(value = "/papers/{paperId}/citation", method = RequestMethod.GET)
    public Map<String, Object> citation(@PathVariable long paperId, @RequestParam CitationFormat format) {
        CitationTextDto dto = paperFacade.citation(paperId, format);

        Map<String, Object> result = new HashMap<>();
        result.put("data", dto);
        return result;
    }

    @RequestMapping(value = "/papers/{paperId}/related", method = RequestMethod.GET)
    public Map<String, Object> related(@PathVariable long paperId) {
        List<PaperDto> related = paperFacade.getRelatedPapers(paperId);

        HashMap<String, Object> result = new HashMap<>();
        Meta meta = related.isEmpty() ? Meta.unavailable() : Meta.available();
        result.put("meta", meta);
        result.put("data", related);

        return result;
    }

    @RequestMapping(value = "/papers/{paperId}/authors/{authorId}/related", method = RequestMethod.GET)
    public HashMap<String, Object> authorRelated(@PathVariable long paperId, @PathVariable long authorId) {
        List<PaperDto> related = paperFacade.getAuthorRelatedPapers(paperId, authorId);

        HashMap<String, Object> result = new HashMap<>();
        Meta meta = related.isEmpty() ? Meta.unavailable() : Meta.available();
        result.put("meta", meta);
        result.put("data", related);

        return result;
    }

}
