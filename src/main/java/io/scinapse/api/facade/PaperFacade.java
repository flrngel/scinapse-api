package io.scinapse.api.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.configuration.AcademicJpaConfig;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.data.academic.Paper;
import io.scinapse.api.dto.CitationTextDto;
import io.scinapse.api.dto.mag.AuthorSearchPaperDto;
import io.scinapse.api.dto.mag.PaperAuthorDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.enums.CitationFormat;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.service.SearchService;
import io.scinapse.api.service.author.AuthorLayerService;
import io.scinapse.api.service.mag.PaperConverter;
import io.scinapse.api.service.mag.PaperService;
import io.scinapse.api.util.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional(readOnly = true, transactionManager = AcademicJpaConfig.ACADEMIC_TX_MANAGER)
@Component
@RequiredArgsConstructor
public class PaperFacade {

    private final SearchService searchService;
    private final PaperService paperService;
    private final AuthorLayerService authorLayerService;
    private final PaperConverter paperConverter;

    public PaperDto find(long paperId, boolean isBot) {
        Paper paper = paperService.find(paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found: " + paperId);
        }

        PaperDto dto = paperConverter.convertSingle(paper, PaperConverter.full());

//        if (!CollectionUtils.isEmpty(dto.getUrls()) && !isBot) {
//            Optional<List<PaperImageDto>> pdfImages = paperPdfImageService.getPdfImages(paperId);
//            if (pdfImages.isPresent()) {
//                dto.setImages(pdfImages.get());
//            } else {
//                paperPdfImageService.extractPdfImagesAsync(paper);
//            }
//        }

        return dto;
    }

    public List<PaperDto> findIn(List<Long> paperIds) {
        return findIn(paperIds, PaperConverter.detail());
    }

    public List<PaperDto> findIn(List<Long> paperIds, PaperConverter.Converter converter) {
        // DO THIS because results from IN query ordered differently
        Map<Long, Paper> map = paperService.findByIdIn(paperIds)
                .stream()
                .collect(Collectors.toMap(
                        Paper::getId,
                        Function.identity()
                ));

        List<Paper> papers = paperIds
                .stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return paperConverter.convert(papers, converter);
    }

    public Map<Long, PaperDto> findMap(List<Long> paperIds, PaperConverter.Converter converter) {
        List<Paper> papers = paperService.findByIdIn(paperIds);
        return paperConverter.convert(papers, converter)
                .stream()
                .collect(Collectors.toMap(
                        PaperDto::getId,
                        Function.identity()
                ));
    }

    public List<PaperDto> convert(List<Paper> papers, PaperConverter.Converter converter) {
        return paperConverter.convert(papers, converter);
    }

    public Page<PaperAuthorDto> getPaperAuthors(long paperId, PageRequest pageRequest) {
        Page<PaperAuthorDto> authorPage = paperService.getPaperAuthors(paperId, pageRequest).map(PaperAuthorDto::new);
        authorLayerService.decoratePaperAuthors(authorPage.getContent());
        return authorPage;
    }

    public Page<PaperDto> findReferences(long paperId, PageRequest pageRequest) {
        Paper paper = paperService.find(paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found: " + paperId);
        }

        List<Long> referenceIds = paperService.findReferences(paperId, pageRequest);
        List<PaperDto> dtos = findIn(referenceIds);
        return new PageImpl<>(dtos, pageRequest.toPageable(), paper.getReferenceCount());
    }

    public Page<PaperDto> findCited(long paperId, PageRequest pageRequest) {
        Paper paper = paperService.find(paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found: " + paperId);
        }

        List<Long> citedIds = paperService.findCited(paperId, pageRequest);
        List<PaperDto> dtos = findIn(citedIds);
        return new PageImpl<>(dtos, pageRequest.toPageable(), paper.getCitationCount());
    }

    public Page<AuthorSearchPaperDto> searchAuthorPaper(Query query, long authorId, PageRequest pageRequest) {
        Page<Long> paperIdPage = searchService.searchAuthorPaper(query, pageRequest);
        List<PaperDto> paperDtos = findIn(paperIdPage.getContent(), PaperConverter.simple());
        List<AuthorSearchPaperDto> dtos = authorLayerService.decorateSearchResult(authorId, paperDtos);
        return new PageImpl<>(dtos, pageRequest.toPageable(), paperIdPage.getTotalElements());
    }

    public CitationTextDto citation(long paperId, CitationFormat format) {
        Paper paper = paperService.find(paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found: " + paperId);
        }

        return paperService.citation(paper.getDoi(), format);
    }

    public List<PaperDto> getRelatedPapers(long paperId) {
        List<Long> relatedPaperIds = paperService.getRelatedPapers(paperId);
        return findIn(relatedPaperIds, PaperConverter.simple());
    }

    public List<PaperDto> getAuthorRelatedPapers(long paperId, long authorId) {
        List<Paper> relatedPapers = paperService.getAuthorRelatedPapers(paperId, authorId);
        return convert(relatedPapers, PaperConverter.simple());
    }

    public List<PaperDto> getReadingNow(long paperId) {
        List<Long> readingNowIds = paperService.getReadingNow(paperId);
        return findIn(readingNowIds, PaperConverter.simple());
    }

}
