package network.pluto.absolute.controller;

import network.pluto.absolute.service.PaperService;
import network.pluto.bibliotheca.academic.Paper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaperController {

    private final PaperService paperService;

    @Autowired
    public PaperController(PaperService paperService) {
        this.paperService = paperService;
    }

    @RequestMapping(value = "/papers/{paperId}", method = RequestMethod.GET)
    public Paper find(@PathVariable long paperId) {
        return paperService.find(paperId);
    }
}
