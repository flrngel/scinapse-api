package network.pluto.absolute.controller;

import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.facade.PaperFacade;
import network.pluto.absolute.util.Query;
import network.pluto.absolute.util.QueryParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaperController {

    private final PaperFacade paperFacade;

    @Autowired
    public PaperController(PaperFacade paperFacade) {
        this.paperFacade = paperFacade;
    }

    @RequestMapping(value = "/papers/{paperId}", method = RequestMethod.GET)
    public PaperDto find(@PathVariable long paperId,
                         @RequestParam(defaultValue = "false") boolean cognitive) {
        if (cognitive) {
            return paperFacade.findFromCognitive(paperId);
        }

        return paperFacade.find(paperId);
    }

    @RequestMapping(value = "/papers", method = RequestMethod.GET)
    public Page<PaperDto> search(@RequestParam("query") String queryStr, @PageableDefault Pageable pageable) {
        if (!StringUtils.hasText(queryStr)) {
            throw new BadRequestException("Invalid query: query not exists");
        }

        Query query = QueryParser.parse(queryStr);
        if (!query.isValid()) {
            throw new BadRequestException("Invalid query: too short query text");
        }

        return paperFacade.search(query, pageable);
    }

    @RequestMapping(value = "/papers", method = RequestMethod.GET, params = "filter")
    public Page<PaperDto> search(@RequestParam("query") String queryStr,
                                 @RequestParam("filter") String filterStr, // TODO not required
                                 @PageableDefault Pageable pageable) {
        Query query = Query.parse(queryStr, filterStr);
        if (!query.isValid()) {
            throw new BadRequestException("Invalid query: too short or long query text");
        }

        return paperFacade.search(query, pageable);
    }

    @RequestMapping(value = "/papers/{paperId}/references", method = RequestMethod.GET)
    public Page<PaperDto> paperReferences(@PathVariable long paperId,
                                          @RequestParam(defaultValue = "false") boolean cognitive,
                                          @PageableDefault Pageable pageable) {
        if (cognitive) {
            return paperFacade.findReferencesFromCognitive(paperId, pageable);
        }

        return paperFacade.findReferences(paperId, pageable);
    }

    @RequestMapping(value = "/papers/{paperId}/cited", method = RequestMethod.GET)
    public Page<PaperDto> paperCited(@PathVariable long paperId,
                                     @RequestParam(defaultValue = "false") boolean cognitive,
                                     @PageableDefault Pageable pageable) {
        if (cognitive) {
            return paperFacade.findCitedFromCognitive(paperId, pageable);
        }

        return paperFacade.findCited(paperId, pageable);
    }

}
