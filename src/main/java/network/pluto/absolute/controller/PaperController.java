package network.pluto.absolute.controller;

import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.facade.PaperFacade;
import network.pluto.absolute.service.PaperService;
import network.pluto.bibliotheca.academic.Paper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaperController {

    private final PaperService paperService;
    private final PaperFacade paperFacade;

    @Autowired
    public PaperController(PaperService paperService, PaperFacade paperFacade) {
        this.paperService = paperService;
        this.paperFacade = paperFacade;
    }

    @RequestMapping(value = "/papers/{paperId}", method = RequestMethod.GET)
    public Paper find(@PathVariable long paperId) {
        return paperService.find(paperId);
    }

    @RequestMapping(value = "/papers/search", method = RequestMethod.GET)
    public Page<PaperDto> search(@RequestParam String text, @PageableDefault Pageable pageable) {
        return paperFacade.search(text, pageable);
    }
}
