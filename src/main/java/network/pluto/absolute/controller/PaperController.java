package network.pluto.absolute.controller;

import network.pluto.absolute.dto.search.PaperSearchDto;
import network.pluto.absolute.service.PaperService;
import network.pluto.absolute.service.SearchService;
import network.pluto.bibliotheca.academic.Paper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PaperController {

    private final PaperService paperService;
    private final SearchService searchService;

    @Autowired
    public PaperController(PaperService paperService, SearchService searchService) {
        this.paperService = paperService;
        this.searchService = searchService;
    }

    @RequestMapping(value = "/papers/{paperId}", method = RequestMethod.GET)
    public Paper find(@PathVariable long paperId) {
        return paperService.find(paperId);
    }

    @RequestMapping(value = "/papers/search", method = RequestMethod.GET)
    public List<PaperSearchDto> search(@RequestParam String text) {
        return searchService.search(text);
    }
}
