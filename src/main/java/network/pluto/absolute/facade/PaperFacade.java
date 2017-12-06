package network.pluto.absolute.facade;

import network.pluto.absolute.configuration.CacheName;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.service.CommentService;
import network.pluto.absolute.service.PaperService;
import network.pluto.absolute.service.SearchService;
import network.pluto.bibliotheca.models.Paper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
    private final CommentService commentService;

    @Autowired
    public PaperFacade(PaperService paperService, SearchService searchService, CommentService commentService) {
        this.paperService = paperService;
        this.searchService = searchService;
        this.commentService = commentService;
    }

    @Cacheable(CacheName.Paper.GET_PAPER)
    public PaperDto find(long paperId) {
        Paper paper = paperService.find(paperId);
        long commentCount = commentService.getCount(paper);

        PaperDto dto = new PaperDto(paper);
        dto.setCommentCount(commentCount);

        return dto;
    }

    @Transactional(readOnly = true)
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
