package io.scinapse.api.controller;

import io.scinapse.api.dto.AuthorDto;
import io.scinapse.api.dto.PaperDto;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.facade.PaperFacade;
import io.scinapse.api.model.mag.Author;
import io.scinapse.api.model.mag.Paper;
import io.scinapse.api.service.mag.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    public Page<PaperDto> getAuthorPapers(@PathVariable long authorId, PageRequest pageRequest) {
        Page<Paper> papers = authorService.getAuthorPaper(authorId, pageRequest);
        List<PaperDto> paperDtos = paperFacade.convert(papers.getContent(), PaperDto.detail());
        return new PageImpl<>(paperDtos, pageRequest.toPageable(), papers.getTotalElements());
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
