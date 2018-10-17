package io.scinapse.api.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.dto.mag.AuthorDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.model.mag.AuthorTopPaper;
import io.scinapse.api.model.mag.Paper;
import io.scinapse.api.service.SearchService;
import io.scinapse.api.service.mag.AuthorService;
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
@Transactional
@Component
@RequiredArgsConstructor
public class AuthorFacade {

    private final SearchService searchService;
    private final AuthorService authorService;
    private final PaperFacade paperFacade;

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
                    List<PaperDto> paperDtos = paperFacade.convert(papers, PaperDto.simple());
                    dto.setTopPapers(paperDtos);

                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(authorDtos, pageRequest.toPageable(), authorIdPage.getTotalElements());
    }

}
