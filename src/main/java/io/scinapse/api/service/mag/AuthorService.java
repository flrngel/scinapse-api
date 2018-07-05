package io.scinapse.api.service.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.dto.PaperAuthorDto;
import io.scinapse.api.dto.PaperDto;
import io.scinapse.api.enums.PaperSort;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.model.mag.Author;
import io.scinapse.api.model.mag.Paper;
import io.scinapse.api.model.mag.PaperAuthorAffiliation;
import io.scinapse.api.repository.mag.AuthorRepository;
import io.scinapse.api.repository.mag.PaperAuthorAffiliationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final PaperAuthorAffiliationRepository repository;

    public Author find(long authorId) {
        return authorRepository.findOne(authorId);
    }

    public Page<Paper> getAuthorPaper(long authorId, PaperSort sort, Pageable pageable) {
        Author author = find(authorId);
        if (author == null) {
            throw new ResourceNotFoundException("Author not found");
        }

        List<Paper> papers = getAuthorPaperList(authorId, sort, pageable);
        return new PageImpl<>(papers, pageable, author.getPaperCount());
    }

    private List<Paper> getAuthorPaperList(long authorId, PaperSort sort, Pageable pageable) {
        if (sort == null) {
            return repository.getAuthorPapersMostCitations(authorId, pageable);
        }

        switch (sort) {
            case NEWEST_FIRST:
                return repository.getAuthorPapersNewest(authorId, pageable);
            case OLDEST_FIRST:
                return repository.getAuthorPapersOldest(authorId, pageable);
            default:
                return repository.getAuthorPapersMostCitations(authorId, pageable);
        }
    }

    public void setDefaultAuthors(List<PaperDto> paperDtos) {
        List<PaperAuthorAffiliation> results = authorRepository.getAuthorsByPaperIdIn(paperDtos.stream().map(PaperDto::getId).collect(Collectors.toList()));
        Map<Long, List<PaperAuthorDto>> authors = results.stream()
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
        return authorRepository.findCoAuthors(authorId);
    }

}
