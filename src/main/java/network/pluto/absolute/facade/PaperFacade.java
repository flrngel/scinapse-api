package network.pluto.absolute.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.AggregationDto;
import network.pluto.absolute.dto.CitationTextDto;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.enums.CitationFormat;
import network.pluto.absolute.enums.PaperSort;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.service.CommentService;
import network.pluto.absolute.service.SearchService;
import network.pluto.absolute.service.mag.AuthorService;
import network.pluto.absolute.service.mag.PaperService;
import network.pluto.absolute.util.Query;
import network.pluto.bibliotheca.models.mag.Paper;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@XRayEnabled
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
            throw new ResourceNotFoundException("Paper not found");
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

    @Transactional(readOnly = true)
    public Page<PaperDto> findReferences(long paperId, Pageable pageable) {
        Paper paper = paperService.find(paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found");
        }

        List<Long> referenceIds = paperService.findReferences(paperId, pageable);
        List<PaperDto> dtos = findIn(referenceIds);
        return new PageImpl<>(dtos, pageable, paper.getPaperCount());
    }

    @Transactional(readOnly = true)
    public Page<PaperDto> findCited(long paperId, Pageable pageable) {
        Paper paper = paperService.find(paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found");
        }

        List<Long> citedIds = paperService.findCited(paperId, pageable);
        List<PaperDto> dtos = findIn(citedIds);
        return new PageImpl<>(dtos, pageable, paper.getCitationCount());
    }

    @Transactional(readOnly = true)
    public Page<PaperDto> search(Query query, Pageable pageable) {
        SearchHit journal = searchService.findJournal(query.getText());
        if (journal != null) {
            query.setJournalSearch(true);
            query.setJournalId(Long.parseLong(journal.getId()));
            return searchByJournal(query, pageable);
        }

        return searchFromES(query, pageable);
    }

    private Page<PaperDto> searchByJournal(Query query, Pageable pageable) {
        return searchFromES(
                query,
                Arrays.asList(
                        SortBuilders.fieldSort("year").order(SortOrder.DESC),
                        SortBuilders.fieldSort("citation_count").order(SortOrder.DESC)),
                pageable);
    }

    public Page<PaperDto> searchFromES(Query query, Pageable pageable) {
        Sort sort = pageable.getSort();
        if (sort != null && sort.iterator().hasNext()) {
            Sort.Order next = sort.iterator().next();
            String property = next.getProperty();
            SortBuilder sortBuilder = PaperSort.toSortBuilder(PaperSort.find(property));
            if (sortBuilder != null) {
                return searchFromES(query, Collections.singletonList(sortBuilder), pageable);
            }
        }

        return searchFromES(query, new ArrayList<>(), pageable);
    }

    public Page<PaperDto> searchFromES(Query query, List<SortBuilder> sorts, Pageable pageable) {
        Page<Long> paperIds;
        if (sorts.isEmpty()) {
            paperIds = searchService.search(query, pageable);
        } else {
            paperIds = searchService.searchWithSort(query, sorts, pageable);
        }
        return convertToDto(paperIds, pageable);
    }

    private Page<PaperDto> convertToDto(Page<Long> paperIds, Pageable pageable) {
        return new PageImpl<>(findIn(paperIds.getContent()), pageable, paperIds.getTotalElements());
    }

    public CitationTextDto citation(long paperId, CitationFormat format) {
        Paper paper = paperService.find(paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found");
        }

        return paperService.citation(paper.getDoi(), format);
    }

    public AggregationDto aggregate(Query query) {
        if (query.isDoi()) {
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

    public List<PaperDto> getRelatedPapers(long paperId) {
        List<Long> relatedPaperIds = paperService.getRelatedPapers(paperId);
        return findIn(relatedPaperIds, PaperDto.simple());
    }

    public List<PaperDto> getAuthorRelatedPapers(long paperId, long authorId) {
        List<Paper> relatedPapers = paperService.getAuthorRelatedPapers(paperId, authorId);
        return convert(relatedPapers, PaperDto.simple());
    }

}
