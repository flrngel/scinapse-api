package network.pluto.absolute.controller;

import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.AuthorDto;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.enums.PaperSort;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.facade.PaperFacade;
import network.pluto.absolute.service.AuthorService;
import network.pluto.bibliotheca.models.mag.Author;
import network.pluto.bibliotheca.models.mag.Paper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;
    private final PaperFacade paperFacade;

    @RequestMapping(value = "/authors/{authorId}", method = RequestMethod.GET)
    public Map<String, Object> find(@PathVariable long authorId) {
        Author author = authorService.find(authorId);
        if (author == null) {
            throw new ResourceNotFoundException("Author not found");
        }

        Map<String, Object> result = new HashMap<>();
        Meta meta = Meta.available();
        result.put("meta", meta);
        result.put("data", new AuthorDto(author));

        return result;
    }

    @RequestMapping(value = "/authors/{authorId}/papers", method = RequestMethod.GET)
    public Page<PaperDto> getAuthorPapers(@PathVariable long authorId, @RequestParam(required = false) PaperSort sort, @PageableDefault Pageable pageable) {
        // FIXME need to create custom pageable converter
        // do this for temporary remove sort from pageable
        PageRequest pageableReplace = new PageRequest(pageable.getPageNumber(), pageable.getPageSize());
        Page<Paper> papers = authorService.getAuthorPaper(authorId, sort, pageableReplace);
        List<PaperDto> paperDtos = paperFacade.convert(papers.getContent(), PaperDto.detail());
        return new PageImpl<>(paperDtos, pageable, papers.getTotalElements());
    }

    @RequestMapping(value = "/authors/{authorId}/co-authors", method = RequestMethod.GET)
    public Map<String, Object> coAuthors(@PathVariable long authorId) {
        Author author = authorService.find(authorId);
        if (author == null) {
            throw new ResourceNotFoundException("Author not found");
        }

        List<AuthorDto> coAuthors = authorService.findCoAuthors(authorId)
                .stream()
                .map(AuthorDto::new)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        Meta meta = coAuthors.isEmpty() ? Meta.unavailable() : Meta.available();
        result.put("meta", meta);
        result.put("data", coAuthors);

        return result;
    }

}
