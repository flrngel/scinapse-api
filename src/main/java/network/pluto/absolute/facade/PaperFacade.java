package network.pluto.absolute.facade;

import network.pluto.absolute.dto.CommentDto;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.service.CommentService;
import network.pluto.absolute.service.PaperService;
import network.pluto.absolute.service.SearchService;
import network.pluto.absolute.util.Query;
import network.pluto.bibliotheca.models.Paper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class PaperFacade {

    private final PaperService paperService;
    private final SearchService searchService;
    private final CommentService commentService;

    @Autowired
    public PaperFacade(PaperService paperService,
                       SearchService searchService,
                       CommentService commentService) {
        this.paperService = paperService;
        this.searchService = searchService;
        this.commentService = commentService;
    }

    @Transactional(readOnly = true)
    public PaperDto find(long paperId) {
        Paper paper = paperService.find(paperId);

        PaperDto dto = new PaperDto(paper);
        dto.setReferenceCount(paperService.countReference(paper.getId()));
        dto.setCitedCount(paperService.countCited(paper.getId()));

        PageRequest pageRequest = new PageRequest(0, 10);
        List<CommentDto> commentDtos = commentService.findByPaper(paper, pageRequest)
                .getContent()
                .stream()
                .map(CommentDto::new)
                .collect(Collectors.toList());
        dto.setComments(commentDtos);

        return dto;
    }

    @Transactional(readOnly = true)
    public Page<PaperDto> findReferences(long paperId, Pageable pageable) {
        Page<Long> referenceIds = paperService.findReferences(paperId, pageable);
        Map<Long, PaperDto> paperMap = findIn(referenceIds.getContent());

        List<PaperDto> dtos = new ArrayList<>();
        referenceIds.getContent().forEach(id -> dtos.add(paperMap.get(id)));

        return new PageImpl<>(dtos, pageable, referenceIds.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<PaperDto> findCited(long paperId, Pageable pageable) {
        Page<Long> citedIds = paperService.findCited(paperId, pageable);
        Map<Long, PaperDto> paperMap = findIn(citedIds.getContent());

        List<PaperDto> dtos = new ArrayList<>();
        citedIds.getContent().forEach(id -> dtos.add(paperMap.get(id)));

        return new PageImpl<>(dtos, pageable, citedIds.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<PaperDto> search(Query query, Pageable pageable) {
        Page<Long> paperIds = searchService.search(query, pageable);
        Map<Long, PaperDto> paperMap = findIn(paperIds.getContent());

        List<PaperDto> dtos = new ArrayList<>();
        paperIds.getContent().forEach(id -> dtos.add(paperMap.get(id)));

        return new PageImpl<>(dtos, pageable, paperIds.getTotalElements());
    }

    private Map<Long, PaperDto> findIn(List<Long> paperIds) {
        List<Paper> papers = paperService.findByIdIn(paperIds);
        return papers
                .stream()
                .filter(Objects::nonNull)
                .map(paper -> {
                    PaperDto dto = new PaperDto(paper);
                    dto.setReferenceCount(paperService.countReference(paper.getId()));
                    dto.setCitedCount(paperService.countCited(paper.getId()));

                    PageRequest pageRequest = new PageRequest(0, 10);
                    List<CommentDto> commentDtos = commentService.findByPaper(paper, pageRequest)
                            .getContent()
                            .stream()
                            .map(CommentDto::new)
                            .collect(Collectors.toList());
                    dto.setComments(commentDtos);

                    return dto;
                })
                .collect(Collectors.toMap(
                        PaperDto::getId,
                        p -> p
                ));
    }
}
