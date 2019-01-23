package io.scinapse.api.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.academic.dto.AcPaperDto;
import io.scinapse.api.academic.service.AcAuthorService;
import io.scinapse.api.academic.service.AcPaperService;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.dto.v2.*;
import io.scinapse.api.service.SearchService;
import io.scinapse.api.service.SearchV2Service;
import io.scinapse.api.service.author.AuthorLayerService;
import io.scinapse.api.util.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional(readOnly = true)
@Component
@RequiredArgsConstructor
public class SearchFacade {

    private final SearchService searchService;
    private final SearchV2Service searchV2Service;
    private final AcAuthorService authorService;
    private final AcPaperService paperService;
    private final AuthorLayerService layerService;

    public EsPaperSearchResponse search(Query query, PageRequest pageRequest) {
        if (query.isDoi()) {
            return searchDoi(query, pageRequest);
        }

        EsPaperSearchResponse response = searchV2Service.search(query, pageRequest, false);

        convertPaperItemPage(response, pageRequest);
        convertAuthorItems(response);

        return response;
    }

    public EsPaperSearchResponse searchDoi(Query query, PageRequest pageRequest) {
        EsPaperSearchResponse response = searchV2Service.searchDoi(query, pageRequest);

        convertPaperItemPage(response, pageRequest);
        return response;
    }

    public Page<AuthorItemDto> searchAuthors(String queryText, PageRequest pageRequest) {
        Page<Long> authorIdPage = searchService.searchAuthor(queryText, pageRequest);

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

        Page<PaperItemDto> page = new PageImpl<>(dtos, pageRequest.toPageable(), response.getPaperTotalHits());
        response.setPaperItemPage(page);
    }

    private void convertAuthorItems(EsPaperSearchResponse response) {
        List<MatchedEntity> matchedAuthors = getAuthorItems(response.getAuthorIds())
                .stream()
                .map(author -> new MatchedEntity(MatchedEntity.MatchedType.AUTHOR, author))
                .collect(Collectors.toList());

        PaperSearchAdditional additional = response.getAdditional();
        additional.getMatchedEntities().addAll(matchedAuthors);
    }

    private List<AuthorItemDto> getAuthorItems(List<Long> authorIds) {
        List<AuthorItemDto> dtos = authorService.findAuthors(authorIds)
                .stream()
                .map(AuthorItemDto::new)
                .collect(Collectors.toList());
        layerService.decorateAuthorItems(dtos);
        return dtos;
    }

}
