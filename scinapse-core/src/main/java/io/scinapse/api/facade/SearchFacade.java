package io.scinapse.api.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.academic.dto.AcAuthorDto;
import io.scinapse.api.academic.dto.AcPaperDto;
import io.scinapse.api.academic.service.AcAuthorService;
import io.scinapse.api.academic.service.AcPaperService;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.dto.v2.*;
import io.scinapse.api.service.CollectionService;
import io.scinapse.api.service.SearchV2Service;
import io.scinapse.api.service.author.AuthorLayerService;
import io.scinapse.api.util.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional(readOnly = true)
@Component
@RequiredArgsConstructor
public class SearchFacade {

    private final SearchV2Service searchV2Service;
    private final AcAuthorService authorService;
    private final AcPaperService paperService;
    private final AuthorLayerService layerService;
    private final CollectionService collectionService;

    public EsPaperSearchResponse search(Query query, PageRequest pageRequest) {
        if (query.isDoi()) {
            return searchByDoi(query, pageRequest);
        }

        EsPaperSearchResponse response = searchV2Service.search(query, pageRequest, false);

        convertPaperItemPage(response, pageRequest);
        convertAuthorItems(response);
        convertTopRefPapers(response);

        return response;
    }

    public EsPaperSearchResponse searchByDoi(Query query, PageRequest pageRequest) {
        EsPaperSearchResponse response = searchV2Service.searchByDoi(query, pageRequest);

        convertPaperItemPage(response, pageRequest);
        return response;
    }

    public EsPaperSearchResponse searchToAdd(Query query, long authorId, PageRequest pageRequest) {
        EsPaperSearchResponse response;
        if (query.isDoi()) {
            response = searchV2Service.searchByDoi(query, pageRequest);
        } else {
            response = searchV2Service.searchToAdd(query, pageRequest);
        }

        convertPaperItemPage(response, pageRequest);
        layerService.decorateAuthorIncluding(response.getPaperItemPage(), authorId);

        return response;
    }

    public EsPaperSearchResponse searchInJournal(Query query, long journalId, PageRequest pageRequest) {
        EsPaperSearchResponse response = searchV2Service.searchInJournal(query, journalId, pageRequest);

        convertPaperItemPage(response, pageRequest);
        return response;
    }

    public Page<AuthorItemDto> searchAuthors(Query query, PageRequest pageRequest) {
        Page<Long> authorIdPage = searchV2Service.searchAuthors(query, pageRequest);

        List<AuthorItemDto> dtos = getAuthorItems(authorIdPage.getContent());
        return new PageImpl<>(dtos, pageRequest.toPageable(), authorIdPage.getTotalElements());
    }

    private void convertPaperItemPage(EsPaperSearchResponse response, PageRequest pageRequest) {
        Set<Long> paperIdSet = response.getEsPapers()
                .stream()
                .map(EsPaperSearchResponse.EsPaper::getPaperId)
                .collect(Collectors.toSet());

        Map<Long, AcPaperDto> paperMap = paperService.findPapers(paperIdSet, AcPaperDto.DetailSelector.detail());

        List<PaperItemDto> dtos = response.getEsPapers()
                .stream()
                .map(paper -> {
                    AcPaperDto acPaperDto = paperMap.get(paper.getPaperId());
                    if (acPaperDto == null) {
                        return null;
                    }

                    // add highlight information
                    PaperItemDto dto = new PaperItemDto(acPaperDto);
                    Optional.ofNullable(paper.getTitleHighlighted()).ifPresent(dto::setTitleHighlighted);
                    Optional.ofNullable(paper.getAbstractHighlighted()).ifPresent(dto::setAbstractHighlighted);

                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<PaperAuthorDto> paperAuthors = dtos
                .stream()
                .map(PaperItemDto::getAuthors)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        layerService.decoratePaperAuthorItems(paperAuthors);

        collectionService.decoratePaperItems(dtos);

        Page<PaperItemDto> page = new PageImpl<>(dtos, pageRequest.toPageable(), response.getPaperTotalHits());
        response.setPaperItemPage(page);
    }

    private void convertAuthorItems(EsPaperSearchResponse response) {
        List<AuthorItemDto> authorItemDtos = getAuthorItems(response.getAuthorIds());

        MatchedAuthor matchedAuthor = new MatchedAuthor(response.getAuthorTotalHits(), authorItemDtos);

        PaperSearchAdditional additional = response.getAdditional();
        additional.setMatchedAuthor(matchedAuthor);
    }

    private List<AuthorItemDto> getAuthorItems(List<Long> authorIds) {
        List<AuthorItemDto> dtos = authorService.findAuthors(authorIds, AcAuthorDto.DetailSelector.full())
                .stream()
                .map(AuthorItemDto::new)
                .collect(Collectors.toList());
        layerService.decorateAuthorItems(dtos);
        return dtos;
    }

    private void convertTopRefPapers(EsPaperSearchResponse response) {
        if (CollectionUtils.isEmpty(response.getTopRefPaperIds())) {
            return;
        }

        List<TopRefPaper> dtos = paperService.findPapers(response.getTopRefPaperIds(), AcPaperDto.DetailSelector.none())
                .stream()
                .map(dto -> {
                    TopRefPaper topRefPaper = new TopRefPaper();
                    topRefPaper.setId(dto.getId());
                    topRefPaper.setTitle(dto.getTitle());
                    topRefPaper.setYear(dto.getYear());
                    topRefPaper.setCitedCount(dto.getCitedCount());
                    return topRefPaper;
                })
                .collect(Collectors.toList());

        response.getAdditional().getTopRefPapers().addAll(dtos);
    }

}
