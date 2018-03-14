package network.pluto.absolute.facade;

import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.CommentDto;
import network.pluto.absolute.dto.JournalDto;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.service.CognitivePaperService;
import network.pluto.absolute.service.CommentService;
import network.pluto.absolute.service.SearchService;
import network.pluto.absolute.service.mag.PaperService;
import network.pluto.absolute.util.Query;
import network.pluto.bibliotheca.models.Comment;
import network.pluto.bibliotheca.models.mag.Paper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaperFacade {

    private final SearchService searchService;
    private final CommentService commentService;
    private final CognitivePaperService cognitivePaperService;
    private final PaperService paperService;

    @Transactional(readOnly = true)
    public PaperDto find(long paperId) {
        Paper paper = paperService.find(paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found");
        }

        PaperDto dto = new PaperDto(paper);

        PageRequest pageRequest = new PageRequest(0, 10);
        Page<Comment> commentPage = commentService.findByPaperId(paperId, pageRequest);
        List<CommentDto> commentDtos = commentPage
                .getContent()
                .stream()
                .map(CommentDto::new)
                .collect(Collectors.toList());

        dto.setCommentCount(commentPage.getTotalElements());
        dto.setComments(commentDtos);
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
    public Page<PaperDto> search(Query query, Pageable pageable) {
        if (query.isDoi()) {
            return searchFromES(query, pageable);
        }

        String recommendedQuery = cognitivePaperService.getRecommendQuery(query);
        if (StringUtils.hasText(recommendedQuery)) {
            return searchFromCognitive(pageable, recommendedQuery);
        }

        return searchFromES(query, pageable);
    }

    private Page<PaperDto> searchFromES(Query query, Pageable pageable) {
        Page<Long> paperIds = searchService.search(query, pageable);
        List<PaperDto> dtoList = findIn(paperIds.getContent());
        Map<Long, PaperDto> paperMap = dtoList.stream().collect(Collectors.toMap(PaperDto::getId, Function.identity()));

        List<PaperDto> dtos = new ArrayList<>();
        paperIds.getContent().forEach(id -> {
            PaperDto dto = paperMap.get(id);
            if (dto != null) {
                JournalDto journal = dto.getJournal();
                if (journal != null) {
                    Double impactFactor = searchService.searchJournalImpact(journal.getFullTitle());
                    journal.setImpactFactor(impactFactor);
                }
                dtos.add(dto);
            }
        });

        return new PageImpl<>(dtos, pageable, paperIds.getTotalElements());
    }

    private Page<PaperDto> searchFromCognitive(Pageable pageable, String recommendedQuery) {
        return cognitivePaperService.search(recommendedQuery, pageable)
                .map(this::setCognitiveDefaultComments);
    }

    private List<PaperDto> findIn(List<Long> paperIds) {
        List<Paper> list = paperService.findByIdIn(paperIds);
        return list
                .stream()
                .filter(Objects::nonNull)
                .map(PaperDto::new)
                .map(this::setCognitiveDefaultComments)
                .collect(Collectors.toList());
    }

    private PaperDto setCognitiveDefaultComments(PaperDto dto) {
        PageRequest pageRequest = new PageRequest(0, 10);
        Page<Comment> commentPage = commentService.findByPaperId(dto.getCognitivePaperId(), pageRequest);
        List<CommentDto> commentDtos = commentPage
                .getContent()
                .stream()
                .map(CommentDto::new)
                .collect(Collectors.toList());

        dto.setCommentCount(commentPage.getTotalElements());
        dto.setComments(commentDtos);
        return dto;
    }

}
