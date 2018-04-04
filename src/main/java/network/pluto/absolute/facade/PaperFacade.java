package network.pluto.absolute.facade;

import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.AggregationDto;
import network.pluto.absolute.dto.CitationTextDto;
import network.pluto.absolute.dto.CommentDto;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.enums.CitationFormat;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.service.CognitivePaperService;
import network.pluto.absolute.service.CommentService;
import network.pluto.absolute.service.SearchService;
import network.pluto.absolute.service.mag.PaperService;
import network.pluto.absolute.util.Query;
import network.pluto.bibliotheca.models.Comment;
import network.pluto.bibliotheca.models.mag.Paper;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaperFacade {

    private final SearchService searchService;
    private final CommentService commentService;
    private final PaperService paperService;
    private final CognitivePaperService cognitivePaperService;

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
    public List<PaperDto> findIn(List<Long> paperIds) {
        // DO THIS because results from IN query ordered randomly
        Map<Long, Paper> map = paperService.findByIdIn(paperIds)
                .stream()
                .collect(Collectors.toMap(
                        Paper::getId,
                        Function.identity()
                ));
        return paperIds
                .stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .map(PaperDto::new)
                .map(this::setDefaultComments)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PaperDto> findReferences(long paperId, Pageable pageable) {
        Paper paper = paperService.find(paperId, false);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found");
        }

        List<Long> referenceIds = paperService.findReferences(paperId, pageable);
        List<PaperDto> dtos = findIn(referenceIds);
        return new PageImpl<>(dtos, pageable, paper.getPaperCount());
    }

    @Transactional(readOnly = true)
    public Page<PaperDto> findCited(long paperId, Pageable pageable) {
        Paper paper = paperService.find(paperId, false);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found");
        }

        List<Long> citedIds = paperService.findCited(paperId, pageable);
        List<PaperDto> dtos = findIn(citedIds);
        return new PageImpl<>(dtos, pageable, paper.getCitationCount());
    }

    @Transactional(readOnly = true)
    public Page<PaperDto> search(Query query, Pageable pageable) {

        // only for title exact match
        String recommendedQuery = cognitivePaperService.getRecommendQuery(query);
        if (StringUtils.hasText(recommendedQuery)) {
            return searchFromCognitive(pageable, recommendedQuery);
        }

        SearchHit journal = searchService.findJournal(query.getText());
        if (journal != null) {
            return searchByJournal(query, pageable);
        }

        return searchFromES(query, pageable);
    }

    public CitationTextDto citation(long paperId, CitationFormat format) {
        Paper paper = paperService.find(paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found");
        }

        return paperService.citation(paper.getDoi(), format);
    }

    private Page<PaperDto> searchByJournal(Query query, Pageable pageable) {
        return searchFromES(
                query,
                Arrays.asList(
                        SortBuilders.fieldSort("year").order(SortOrder.DESC),
                        SortBuilders.fieldSort("citation_count").order(SortOrder.DESC)),
                pageable);
    }

    private Page<PaperDto> searchFromES(Query query, Pageable pageable) {
        return searchFromES(query, new ArrayList<>(), pageable);
    }

    private Page<PaperDto> searchFromES(Query query, List<SortBuilder> sorts, Pageable pageable) {
        Page<Long> paperIds = searchService.search(query, sorts, pageable);
        return convertToDto(paperIds, pageable);
    }

    private Page<PaperDto> searchFromCognitive(Pageable pageable, String recommendedQuery) {
        Page<Long> paperIds = cognitivePaperService.search(recommendedQuery, pageable);
        return convertToDto(paperIds, pageable);
    }

    private Page<PaperDto> convertToDto(Page<Long> paperIds, Pageable pageable) {
        return new PageImpl<>(findIn(paperIds.getContent()), pageable, paperIds.getTotalElements());
    }

    private PaperDto setDefaultComments(PaperDto dto) {
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

    public AggregationDto aggregate(Query query) {
        if (query.isDoi()) {
            return AggregationDto.unavailable();
        }

        // only for title exact match
        String recommendedQuery = cognitivePaperService.getRecommendQuery(query);
        if (StringUtils.hasText(recommendedQuery)) {
            return AggregationDto.unavailable();
        }

        SearchHit journal = searchService.findJournal(query.getText());
        if (journal != null) {
            return AggregationDto.unavailable();
        }

        AggregationDto dto = searchService.aggregateFromSample(query);

        // for calculate doc count for each buckets
        searchService.enhanceAggregation(query, dto);

        return dto;
    }

}
