package network.pluto.absolute.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.RequiredArgsConstructor;
import network.pluto.absolute.enums.PaperSort;
import network.pluto.bibliotheca.models.mag.Author;
import network.pluto.bibliotheca.models.mag.Paper;
import network.pluto.bibliotheca.repositories.mag.AuthorRepository;
import network.pluto.bibliotheca.repositories.mag.PaperAuthorAffiliationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
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

    public List<Author> findCoAuthors(long authorId) {
        List<Long> coAuthorIds = repository.findCoAuthors(authorId)
                .stream()
                .map(BigInteger::longValue)
                .collect(Collectors.toList());
        return authorRepository.findByIdIn(coAuthorIds);
    }

}
