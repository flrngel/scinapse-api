package io.scinapse.api.controller;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.dto.PaperTitleDto;
import io.scinapse.api.dto.mag.AuthorDto;
import io.scinapse.api.dto.mag.AuthorLayerUpdateDto;
import io.scinapse.api.dto.mag.AuthorPaperDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.dto.response.Response;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.facade.AuthorFacade;
import io.scinapse.api.facade.MemberFacade;
import io.scinapse.api.model.Member;
import io.scinapse.api.security.jwt.JwtUser;
import io.scinapse.api.service.mag.AuthorService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;
    private final AuthorFacade authorFacade;
    private final MemberFacade memberFacade;

    @RequestMapping(value = "/authors/{authorId}", method = RequestMethod.GET)
    public Map<String, Object> find(@PathVariable long authorId) {
        AuthorDto dto = authorFacade.findDetailed(authorId);

        Map<String, Object> result = new HashMap<>();
        Meta meta = Meta.available();
        result.put("meta", meta);
        result.put("data", dto);

        return result;
    }

    @RequestMapping(value = "/authors/{authorId}/papers", method = RequestMethod.GET)
    public Page<AuthorPaperDto> findPapers(@PathVariable long authorId,
                                           @RequestParam(value = "query", required = false) String queryStr,
                                           PageRequest pageRequest) {
        String query = StringUtils.normalizeSpace(queryStr);
        String[] keywords = StringUtils.split(query);

        return authorFacade.findPapers(authorId, keywords, pageRequest);
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

    @RequestMapping(value = "/authors/{authorId}/connect", method = RequestMethod.POST)
    public Response connect(JwtUser user, @PathVariable long authorId) {
        Member member = memberFacade.loadMember(user);
        authorFacade.connect(member, authorId);
        return Response.success();
    }

    @RequestMapping(value = "/authors/{authorId}/papers/remove", method = RequestMethod.POST)
    public Response removePapers(JwtUser user,
                                 @PathVariable long authorId,
                                 @RequestBody @Valid PaperIdWrapper wrapper) {
        Member member = memberFacade.loadMember(user);
        authorFacade.removePapers(member, authorId, wrapper.getPaperIds());
        return Response.success();
    }

    @RequestMapping(value = "/authors/{authorId}/papers/add", method = RequestMethod.POST)
    public Response addPapers(JwtUser user,
                              @PathVariable long authorId,
                              @RequestBody @Valid PaperIdWrapper wrapper) {
        Member member = memberFacade.loadMember(user);
        authorFacade.addPapers(member, authorId, wrapper.getPaperIds());
        return Response.success();
    }

    @RequestMapping(value = "/authors/{authorId}/papers/selected", method = RequestMethod.PUT)
    public Response<List<PaperDto>> updateSelected(JwtUser user,
                                                   @PathVariable long authorId,
                                                   @RequestBody PaperIdWrapper wrapper) {
        Member member = memberFacade.loadMember(user);
        return Response.success(authorFacade.updateSelected(member, authorId, wrapper.getPaperIds()));
    }

    @RequestMapping(value = "/authors/{authorId}", method = RequestMethod.PUT)
    public Response<AuthorDto> updateAuthorInformation(JwtUser user,
                                                       @PathVariable long authorId,
                                                       @RequestBody @Valid AuthorLayerUpdateDto updateDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(authorFacade.update(member, authorId, updateDto));
    }

    @RequestMapping(value = "/authors/{authorId}/papers/all", method = RequestMethod.GET)
    public Response<List<PaperTitleDto>> getAllPaperTitles(@PathVariable long authorId) {
        return Response.success(authorFacade.getAllPaperTitles(authorId));
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Getter
    @Setter
    private static class PaperIdWrapper {
        @Size(min = 1)
        @NotNull
        private List<Long> paperIds;
    }

}
