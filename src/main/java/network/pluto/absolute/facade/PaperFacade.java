package network.pluto.absolute.facade;

import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.service.PaperService;
import network.pluto.absolute.service.SearchService;
import network.pluto.bibliotheca.models.Paper;
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

    @Transactional(readOnly = true)
    public PaperDto find(long paperId) {
        Paper paper = paperService.find(paperId);

        PaperDto dto = new PaperDto(paper);
        dto.setReferenceCount(paperService.countReference(paper.getId()));
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<PaperDto> findReferences(long paperId, Pageable pageable) {
        Page<Long> referenceIds = paperService.findReferences(paperId, pageable);
        return findIn(referenceIds);
    }

    @Transactional(readOnly = true)
    public Page<PaperDto> findCited(long paperId, Pageable pageable) {
        Page<Long> citedIds = paperService.findCited(paperId, pageable);
        return findIn(citedIds);
    }

    @Transactional(readOnly = true)
    public Page<PaperDto> search(String text, Pageable pageable) {
        Page<Long> paperIds = searchService.search(text, pageable);
        return findIn(paperIds);
    }

    private Page<PaperDto> findIn(Page<Long> paperIds) {
        List<Paper> papers = paperService.findByIdIn(paperIds.getContent());
        Map<Long, Paper> paperMap = papers.stream().collect(Collectors.toMap(Paper::getId, p -> p));

        return paperIds.map(s -> {
            Paper paper = paperMap.get(s);
            if (paper == null) {
                return null;
            }

            PaperDto dto = new PaperDto(paper);
            dto.setReferenceCount(paperService.countReference(paper.getId()));
            return dto;
        });
    }
}
