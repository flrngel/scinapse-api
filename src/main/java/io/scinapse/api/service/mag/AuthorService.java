package io.scinapse.api.service.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.dto.mag.PaperAuthorDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.enums.PaperSort;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.model.mag.*;
import io.scinapse.api.repository.mag.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final PaperAuthorRepository paperAuthorRepository;
    private final PaperTopAuthorRepository paperTopAuthorRepository;
    private final AuthorCoauthorRepository authorCoauthorRepository;
    private final AuthorTopPaperRepository authorTopPaperRepository;

    public boolean exists(long authorId) {
        return authorRepository.exists(authorId);
    }

    public Optional<Author> find(long authorId) {
        return Optional.ofNullable(authorRepository.findOne(authorId));
    }

    public Page<Paper> getAuthorPaper(long authorId, PageRequest pageRequest) {
        Author author = find(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found: " + authorId));

        List<Paper> papers = getAuthorPaperList(authorId, pageRequest);
        return new PageImpl<>(papers, pageRequest.toPageable(), author.getPaperCount());
    }

    private List<Paper> getAuthorPaperList(long authorId, PageRequest pageRequest) {
        PaperSort sort = PaperSort.find(pageRequest.getSort());
        if (sort == null) {
            return paperAuthorRepository.getAuthorPapersMostCitations(authorId, pageRequest.toPageable());
        }

        switch (sort) {
            case NEWEST_FIRST:
                return paperAuthorRepository.getAuthorPapersNewest(authorId, pageRequest.toPageable());
            case OLDEST_FIRST:
                return paperAuthorRepository.getAuthorPapersOldest(authorId, pageRequest.toPageable());
            default:
                return paperAuthorRepository.getAuthorPapersMostCitations(authorId, pageRequest.toPageable());
        }
    }

    public Page<Paper> getAuthorPaper(List<Long> authorIds, PageRequest pageRequest) {
        Page<PaperAuthor> authorPapers = paperAuthorRepository.getByAuthorIdInOrderByPaperCitationCountDesc(authorIds, pageRequest.toPageable());
        List<Paper> papers = authorPapers.getContent().stream().map(PaperAuthor::getPaper).collect(Collectors.toList());
        return pageRequest.toPage(papers, authorPapers.getTotalElements());
    }

    public void setDefaultAuthors(List<PaperDto> paperDtos) {
        if (CollectionUtils.isEmpty(paperDtos)) {
            return;
        }

        List<PaperTopAuthor> results = paperTopAuthorRepository.findByPaperIdIn(paperDtos.stream().map(PaperDto::getId).collect(Collectors.toList()));

        Map<Long, List<PaperAuthorDto>> authors = results.stream()
                .filter(pa -> pa.getAuthor() != null) // prevent exception, due to ghost authors
                .map(PaperAuthorDto::new)
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(PaperAuthorDto::getPaperId),
                        map -> {
                            map.values().forEach(list -> list.sort(Comparator.comparing(PaperAuthorDto::getOrder)));
                            return map;
                        }));
        paperDtos.forEach(p -> {
            List<PaperAuthorDto> authorDtos = authors.get(p.getId());
            if (authorDtos != null) {
                p.setAuthors(authorDtos);
            }
        });
    }

    public void setDefaultAuthors(PaperDto paperDto) {
        setDefaultAuthors(Collections.singletonList(paperDto));
    }

    public List<Author> findCoAuthors(long authorId) {
        return authorCoauthorRepository.findByAuthorId(authorId).stream()
                .map(AuthorCoauthor::getCoauthor)
                .collect(Collectors.toList());
    }

    public List<AuthorTopPaper> findAuthorTopPaper(List<Long> authorIds) {
        return authorTopPaperRepository.findByAuthorIdIn(authorIds);
    }

}
