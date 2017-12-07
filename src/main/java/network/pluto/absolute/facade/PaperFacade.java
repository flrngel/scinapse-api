package network.pluto.absolute.facade;

import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.service.PaperService;
import network.pluto.absolute.service.SearchService;
import network.pluto.bibliotheca.models.Paper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
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
        List<PaperDto> dtos = findIn(referenceIds.getContent());
        return new PageImpl<>(dtos, pageable, referenceIds.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<PaperDto> findCited(long paperId, Pageable pageable) {
        Page<Long> citedIds = paperService.findCited(paperId, pageable);
        List<PaperDto> dtos = findIn(citedIds.getContent());
        return new PageImpl<>(dtos, pageable, citedIds.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<PaperDto> search(String text, Pageable pageable) {
        Page<Long> paperIds = searchService.search(text, pageable);
        List<PaperDto> dtos = findIn(paperIds.getContent());
        return new PageImpl<>(dtos, pageable, paperIds.getTotalElements());
    }

    private List<PaperDto> findIn(List<Long> paperIds) {
        List<Paper> papers = paperService.findByIdIn(paperIds);
        return papers
                .stream()
                .filter(Objects::nonNull)
                .map(paper -> {
                    PaperDto dto = new PaperDto(paper);
                    dto.setReferenceCount(paperService.countReference(paper.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
