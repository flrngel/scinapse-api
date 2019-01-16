package io.scinapse.api.service.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.configuration.AcademicJpaConfig;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.data.academic.Author;
import io.scinapse.api.data.academic.AuthorCoauthor;
import io.scinapse.api.data.academic.AuthorTopPaper;
import io.scinapse.api.data.academic.Paper;
import io.scinapse.api.data.academic.repository.AuthorCoauthorRepository;
import io.scinapse.api.data.academic.repository.AuthorRepository;
import io.scinapse.api.data.academic.repository.AuthorTopPaperRepository;
import io.scinapse.api.data.academic.repository.PaperAuthorRepository;
import io.scinapse.api.dto.mag.AuthorDto;
import io.scinapse.api.enums.PaperSort;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.service.author.AuthorLayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional(readOnly = true, transactionManager = AcademicJpaConfig.ACADEMIC_TX_MANAGER)
@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final PaperAuthorRepository paperAuthorRepository;
    private final AuthorCoauthorRepository authorCoauthorRepository;
    private final AuthorTopPaperRepository authorTopPaperRepository;
    private final AuthorLayerService layerService;

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

    public List<AuthorDto> findCoAuthors(long authorId) {
        List<AuthorDto> dtos = authorCoauthorRepository.findByAuthorId(authorId)
                .stream()
                .sorted(Comparator.comparing(AuthorCoauthor::getRank, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(AuthorCoauthor::getCoauthor)
                .map(AuthorDto::new)
                .collect(Collectors.toList());
        return layerService.decorateAuthors(dtos);
    }

    public List<AuthorTopPaper> findAuthorTopPaper(List<Long> authorIds) {
        return authorTopPaperRepository.findByAuthorIdIn(authorIds);
    }

}
