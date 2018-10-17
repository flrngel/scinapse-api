package io.scinapse.api.controller;

import io.scinapse.api.dto.mag.AuthorDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.dto.response.Response;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.facade.AuthorFacade;
import io.scinapse.api.facade.PaperFacade;
import io.scinapse.api.model.mag.Author;
import io.scinapse.api.model.mag.Paper;
import io.scinapse.api.service.mag.AuthorService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    private final AuthorFacade authorFacade;

    @RequestMapping(value = "/authors/{authorId}", method = RequestMethod.GET)
    public Map<String, Object> find(@PathVariable long authorId) {
        Author author = authorService.find(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found: " + authorId));

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
        if (!authorService.exists(authorId)) {
            throw new ResourceNotFoundException("Author not found: " + authorId);
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

    @RequestMapping(value = "/authors", method = RequestMethod.GET)
    public Response<List<AuthorDto>> searchAuthor(@RequestParam("query") String queryStr, PageRequest pageRequest) {
        String keyword = StringUtils.normalizeSpace(queryStr);
        if (StringUtils.length(keyword) < 2 || StringUtils.length(keyword) > 100) {
            throw new BadRequestException("Invalid query: too short or long query text");
        }
        return Response.success(authorFacade.searchAuthor(keyword, pageRequest));
    }

}
