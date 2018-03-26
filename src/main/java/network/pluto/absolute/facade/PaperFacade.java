package network.pluto.absolute.facade;

import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.AggregationDto;
import network.pluto.absolute.dto.CommentDto;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.service.CognitivePaperService;
import network.pluto.absolute.service.CommentService;
import network.pluto.absolute.service.SearchService;
import network.pluto.absolute.service.mag.PaperService;
import network.pluto.absolute.util.Query;
import network.pluto.bibliotheca.models.Comment;
import network.pluto.bibliotheca.models.mag.Paper;
import org.elasticsearch.index.query.QueryBuilder;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
            return searchFromES(query.toQuery(), pageable);
        }

        SearchHit journal = searchService.findJournal(query.getText());
        if (journal != null) {
            return searchByJournal(query, pageable);
        }

        String recommendedQuery = cognitivePaperService.getRecommendQuery(query);
        if (StringUtils.hasText(recommendedQuery)) {
            return searchFromCognitive(pageable, recommendedQuery);
        }

        return searchFromES(query.toQuery(), pageable);
    }

    private Page<PaperDto> searchByJournal(Query query, Pageable pageable) {
        return searchFromES(
                query.toJournalQuery(),
                Arrays.asList(
                        SortBuilders.fieldSort("year").order(SortOrder.DESC),
                        SortBuilders.fieldSort("citation_count").order(SortOrder.DESC)),
                pageable);
    }

    private Page<PaperDto> searchFromES(QueryBuilder query, Pageable pageable) {
        return searchFromES(query, new ArrayList<>(), pageable);
    }

    private Page<PaperDto> searchFromES(QueryBuilder query, List<SortBuilder> sorts, Pageable pageable) {
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

    private List<PaperDto> findIn(List<Long> paperIds) {
        List<Paper> list = paperService.findByIdIn(paperIds);
        return list
                .stream()
                .filter(Objects::nonNull)
                .map(PaperDto::new)
                .map(this::setDefaultComments)
                .collect(Collectors.toList());
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
        return searchService.aggregate(query.toAggregationQuery());
    }

}
