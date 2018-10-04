package io.scinapse.api.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.dto.AggregationDto;
import io.scinapse.api.dto.CitationTextDto;
import io.scinapse.api.dto.mag.PaperAuthorDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.enums.CitationFormat;
import io.scinapse.api.enums.PaperSort;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.model.mag.Paper;
import io.scinapse.api.model.mag.PaperAuthor;
import io.scinapse.api.service.CommentService;
import io.scinapse.api.service.SearchService;
import io.scinapse.api.service.mag.AuthorService;
import io.scinapse.api.service.mag.PaperService;
import io.scinapse.api.util.Query;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional
@Component
@RequiredArgsConstructor
public class PaperFacade {

    private final SearchService searchService;
    private final CommentService commentService;
    private final PaperService paperService;
    private final AuthorService authorService;

    public PaperDto find(long paperId) {
        Paper paper = paperService.find(paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found: " + paperId);
        }

        PaperDto dto = PaperDto.full().convert(paper);
        authorService.setDefaultAuthors(dto);
        commentService.setDefaultComments(dto);
        return dto;
    }

    public List<PaperDto> findIn(List<Long> paperIds) {
        return findIn(paperIds, PaperDto.detail());
    }

    public List<PaperDto> findIn(List<Long> paperIds, PaperDto.Converter converter) {
        // DO THIS because results from IN query ordered randomly
        Map<Long, Paper> map = paperService.findByIdIn(paperIds)
                .stream()
                .collect(Collectors.toMap(
                        Paper::getId,
                        Function.identity()
                ));

        List<PaperDto> dtos = paperIds
                .stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .map(converter::convert)
                .collect(Collectors.toList());

        authorService.setDefaultAuthors(dtos);
        return dtos;
    }

    public List<PaperDto> convert(List<Paper> papers, PaperDto.Converter converter) {
        List<PaperDto> dtos = papers
                .stream()
                .map(converter::convert)
                .collect(Collectors.toList());
        authorService.setDefaultAuthors(dtos);
        return dtos;
    }

    public Page<PaperAuthorDto> getPaperAuthors(long paperId, PageRequest pageRequest) {
        Page<PaperAuthor> paperAuthors = paperService.getPaperAuthors(paperId, pageRequest);
        return paperAuthors.map(PaperAuthorDto::new);
    }

    @Transactional(readOnly = true)
    public Page<PaperDto> findReferences(long paperId, PageRequest pageRequest) {
        Paper paper = paperService.find(paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found: " + paperId);
        }

        List<Long> referenceIds = paperService.findReferences(paperId, pageRequest);
        List<PaperDto> dtos = findIn(referenceIds);
        return new PageImpl<>(dtos, pageRequest.toPageable(), paper.getPaperCount());
    }

    @Transactional(readOnly = true)
    public Page<PaperDto> findCited(long paperId, PageRequest pageRequest) {
        Paper paper = paperService.find(paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found: " + paperId);
        }

        List<Long> citedIds = paperService.findCited(paperId, pageRequest);
        List<PaperDto> dtos = findIn(citedIds);
        return new PageImpl<>(dtos, pageRequest.toPageable(), paper.getCitationCount());
    }

    @Transactional(readOnly = true)
    public Page<PaperDto> search(Query query, PageRequest pageRequest) {
        return searchFromES(query, pageRequest);
    }

    public Page<PaperDto> searchFromES(Query query, PageRequest pageRequest) {
        String sort = pageRequest.getSort();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(sort)) {
            SortBuilder sortBuilder = PaperSort.toSortBuilder(sort);
            if (sortBuilder != null) {
                return searchFromES(query, Collections.singletonList(sortBuilder), pageRequest);
            }
        }

        return searchFromES(query, new ArrayList<>(), pageRequest);
    }

    public Page<PaperDto> searchFromES(Query query, List<SortBuilder> sorts, PageRequest pageRequest) {
        Page<Long> paperIds;
        if (sorts.isEmpty()) {
            paperIds = searchService.search(query, pageRequest);
        } else {
            paperIds = searchService.searchWithSort(query, sorts, pageRequest);
        }
        return convertToDto(paperIds, pageRequest);
    }

    private Page<PaperDto> convertToDto(Page<Long> paperIds, PageRequest pageRequest) {
        return new PageImpl<>(findIn(paperIds.getContent()), pageRequest.toPageable(), paperIds.getTotalElements());
    }

    public CitationTextDto citation(long paperId, CitationFormat format) {
        Paper paper = paperService.find(paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found: " + paperId);
        }

        return paperService.citation(paper.getDoi(), format);
    }

    public AggregationDto aggregate(Query query) {
        if (query.isDoi()) {
            return AggregationDto.unavailable();
        }

        AggregationDto dto = searchService.aggregateFromSample(query);

        // for calculate doc count for each buckets
        searchService.enhanceAggregation(query, dto);

        return dto;
    }

    public List<PaperDto> getRelatedPapers(long paperId) {
        List<Long> relatedPaperIds = paperService.getRelatedPapers(paperId);
        return findIn(relatedPaperIds, PaperDto.simple());
    }

    public List<PaperDto> getAuthorRelatedPapers(long paperId, long authorId) {
        List<Paper> relatedPapers = paperService.getAuthorRelatedPapers(paperId, authorId);
        return convert(relatedPapers, PaperDto.simple());
    }

}
