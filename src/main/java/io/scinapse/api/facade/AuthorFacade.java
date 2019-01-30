package io.scinapse.api.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.configuration.AcademicJpaConfig;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.data.academic.AuthorTopPaper;
import io.scinapse.api.data.academic.Paper;
import io.scinapse.api.data.scinapse.model.author.AuthorLayerPaper;
import io.scinapse.api.dto.mag.AuthorDto;
import io.scinapse.api.dto.mag.AuthorPaperDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.service.SearchService;
import io.scinapse.api.service.mag.AuthorService;
import io.scinapse.api.service.mag.PaperConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional(readOnly = true, transactionManager = AcademicJpaConfig.ACADEMIC_TX_MANAGER)
@Component
@RequiredArgsConstructor
public class AuthorFacade {

    private final SearchService searchService;
    private final AuthorService authorService;
    private final PaperFacade paperFacade;
    private final PaperConverter paperConverter;

    public Page<AuthorDto> searchAuthor(String keyword, PageRequest pageRequest) {
        // author search from ES
        Page<Long> authorIdPage = searchService.searchAuthor(keyword, pageRequest);

        // get detail from RDB
        // automatically filter authors who have no paper at all
        List<AuthorTopPaper> authorTopPapers = authorService.findAuthorTopPaper(authorIdPage.getContent());

        // transform to author dto
        Map<Long, List<AuthorTopPaper>> map = authorTopPapers.stream().collect(Collectors.groupingBy(ap -> ap.getId().getAuthorId()));

        List<AuthorDto> authorDtos = authorIdPage.getContent()
                .stream()
                .map(id -> {
                    List<AuthorTopPaper> topPapers = map.get(id);
                    if (CollectionUtils.isEmpty(topPapers)) {
                        return null;
                    }

                    AuthorDto dto = new AuthorDto(topPapers.get(0).getAuthor());

                    List<Paper> papers = topPapers.stream().map(AuthorTopPaper::getPaper).collect(Collectors.toList());
                    List<PaperDto> paperDtos = paperFacade.convert(papers, PaperConverter.simple());
                    dto.setTopPapers(paperDtos);

                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(authorDtos, pageRequest.toPageable(), authorIdPage.getTotalElements());
    }

    public Page<AuthorPaperDto> findPapers(long authorId, PageRequest pageRequest) {
        Page<Paper> paperPage = authorService.getAuthorPaper(authorId, pageRequest);

        List<AuthorPaperDto> paperDtos = paperConverter.convert(paperPage.getContent(), PaperConverter.detail())
                .stream()
                .map(dto -> new AuthorPaperDto(dto, AuthorLayerPaper.PaperStatus.SYNCED, false))
                .collect(Collectors.toList());

        return new PageImpl<>(paperDtos, pageRequest.toPageable(), paperPage.getTotalElements());
    }

    public AuthorDto find(long authorId, boolean loadFos) {
        return authorService.find(authorId)
                .map(author -> new AuthorDto(author, loadFos))
                .orElseThrow(() -> new BadRequestException("Author[" + authorId + "] does not exist."));
    }

}
