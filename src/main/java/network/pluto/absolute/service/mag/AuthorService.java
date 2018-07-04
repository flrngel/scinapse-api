package network.pluto.absolute.service.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.PaperAuthorDto;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.enums.PaperSort;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.bibliotheca.dtos.AffiliationDto;
import network.pluto.bibliotheca.dtos.AuthorDto;
import network.pluto.bibliotheca.models.mag.Author;
import network.pluto.bibliotheca.models.mag.Paper;
import network.pluto.bibliotheca.models.mag.PaperAuthorAffiliation;
import network.pluto.bibliotheca.repositories.mag.AuthorRepository;
import network.pluto.bibliotheca.repositories.mag.PaperAuthorAffiliationRepository;
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
        Map<Long, List<AuthorDto>> authors = results.stream()
                .map(r -> {
                    AuthorDto authorDto = new AuthorDto();
                    authorDto.setPaperId(r.getId().getPaperId());
                    authorDto.setOrder(r.getAuthorSequenceNumber());
                    authorDto.setId(r.getId().getAuthorId());
                    authorDto.setName(r.getAuthor().getDisplayName());

                    if (r.getAuthor().getAuthorHIndex() != null) {
                        authorDto.setHIndex(r.getAuthor().getAuthorHIndex().getHIndex());
                    }

                    if (r.getAffiliation() != null) {
                        AffiliationDto affiliationDto = new AffiliationDto();
                        affiliationDto.setId(r.getAffiliation().getId());
                        affiliationDto.setName(r.getAffiliation().getDisplayName());
                        authorDto.setAffiliation(affiliationDto);
                    }

                    return authorDto;
                })
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(AuthorDto::getPaperId),
                        map -> {
                            map.values().forEach(list -> list.sort(Comparator.comparing(AuthorDto::getOrder)));
                            return map;
                        }));
        paperDtos.forEach(p -> {
            List<AuthorDto> authorDtos = authors.get(p.getId());
            if (authorDtos != null) {
                p.setAuthors(authorDtos.stream().map(PaperAuthorDto::new).collect(Collectors.toList()));
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
