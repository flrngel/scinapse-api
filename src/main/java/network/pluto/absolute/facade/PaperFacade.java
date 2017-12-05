package network.pluto.absolute.facade;

import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.service.PaperService;
import network.pluto.absolute.service.SearchService;
import network.pluto.bibliotheca.academic.Paper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PaperFacade {

    private final PaperService paperService;
    private final SearchService searchService;

    @Autowired
    public PaperFacade(PaperService paperService, SearchService searchService) {
        this.paperService = paperService;
        this.searchService = searchService;
    }

    @Transactional
    public Page<PaperDto> search(String text, Pageable pageable) {
        Page<Long> search = searchService.search(text, pageable);
        List<Paper> papers = paperService.findByIdIn(search.getContent());
        Map<Long, Paper> paperMap = papers.stream().collect(Collectors.toMap(Paper::getId, p -> p));

        return search.map(s -> {
            Paper paper = paperMap.get(s);
            if (paper == null) {
                return null;
            }
            return new PaperDto(paper);
        });
    }
}
