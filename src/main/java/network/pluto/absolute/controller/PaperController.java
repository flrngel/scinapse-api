package network.pluto.absolute.controller;

import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.AggregationDto;
import network.pluto.absolute.dto.CitationTextDto;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.enums.CitationFormat;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.facade.PaperFacade;
import network.pluto.absolute.util.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

}
