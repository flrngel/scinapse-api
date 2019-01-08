package io.scinapse.api.controller;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.data.scinapse.model.Member;
import io.scinapse.api.dto.PaperTitleDto;
import io.scinapse.api.dto.mag.AuthorDto;
import io.scinapse.api.dto.mag.AuthorLayerUpdateDto;
import io.scinapse.api.dto.mag.AuthorPaperDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.dto.response.Response;
import io.scinapse.api.enums.AuthorityName;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.facade.AuthorFacade;
import io.scinapse.api.facade.AuthorLayerFacade;
import io.scinapse.api.facade.MemberFacade;
import io.scinapse.api.security.jwt.JwtUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorFacade authorFacade;
    private final AuthorLayerFacade authorLayerFacade;
    private final MemberFacade memberFacade;

    private final Environment environment;

    @RequestMapping(value = "/authors/{authorId}", method = RequestMethod.GET)
    public Map<String, Object> find(JwtUser user, @PathVariable long authorId) {
        boolean includeEmail = false;
        if (user != null) {
            Member member = memberFacade.loadMember(user);
            if (member.getAuthorId() != null && member.getAuthorId() == authorId) {
                includeEmail = true;
            }
        }
        AuthorDto dto = authorLayerFacade.findDetailed(authorId, includeEmail);

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

        return authorLayerFacade.findPapers(authorId, keywords, pageRequest);
    }

    @RequestMapping(value = "/authors/{authorId}/co-authors", method = RequestMethod.GET)
    public Map<String, Object> coAuthors(@PathVariable long authorId) {
        List<AuthorDto> coAuthors = authorLayerFacade.findCoAuthors(authorId);

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
    public Response<AuthorDto> connect(JwtUser user, @PathVariable long authorId, @RequestBody @Valid AuthorLayerUpdateDto dto) {
        Member member = memberFacade.loadMember(user);
        AuthorDto connectedDto = authorLayerFacade.connect(member, authorId, dto);

        return Response.success(connectedDto);
    }

    @RequestMapping(value = "/authors/{authorId}/disconnect", method = RequestMethod.GET)
    public Response disconnect(@PathVariable long authorId, JwtUser user) {
        if (environment.acceptsProfiles("prod")) {
            if (user == null || CollectionUtils.isEmpty(user.getAuthorities())) {
                throw new AccessDeniedException("Login is required.");
            }
            if (!isAdmin(user)) {
                throw new AccessDeniedException("Access is denied. Only admin user can access.");
            }
        }

        authorLayerFacade.disconnect(authorId);
        return Response.success();
    }

    private boolean isAdmin(JwtUser user) {
        return user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> StringUtils.equalsIgnoreCase(auth, AuthorityName.ROLE_ADMIN.name()));
    }

    @RequestMapping(value = "/authors/{authorId}/papers/remove", method = RequestMethod.POST)
    public Response removePapers(JwtUser user,
                                 @PathVariable long authorId,
                                 @RequestBody PaperIdWrapper wrapper) {
        if (wrapper.paperIds.size() < 1) {
            throw new BadRequestException("No papers selected.");
        }

        Member member = memberFacade.loadMember(user);
        authorLayerFacade.removePapers(member, isAdmin(user), authorId, wrapper.getPaperIds());
        return Response.success();
    }

    @RequestMapping(value = "/authors/{authorId}/papers/add", method = RequestMethod.POST)
    public Response addPapers(JwtUser user,
                              @PathVariable long authorId,
                              @RequestBody PaperIdWrapper wrapper) {
        if (wrapper.paperIds.size() < 1) {
            throw new BadRequestException("No papers selected.");
        }

        Member member = memberFacade.loadMember(user);
        authorLayerFacade.addPapers(member, isAdmin(user), authorId, wrapper.getPaperIds());
        return Response.success();
    }

    @RequestMapping(value = { "/authors/{authorId}/papers/selected", "/authors/{authorId}/papers/representative" }, method = RequestMethod.PUT)
    public Response<List<PaperDto>> updateRepresentative(JwtUser user,
                                                         @PathVariable long authorId,
                                                         @RequestBody PaperIdWrapper wrapper) {
        Member member = memberFacade.loadMember(user);
        return Response.success(authorLayerFacade.updateRepresentative(member, authorId, wrapper.getPaperIds()));
    }

    @RequestMapping(value = "/authors/{authorId}", method = RequestMethod.PUT)
    public Response<AuthorDto> updateAuthorInformation(JwtUser user,
                                                       @PathVariable long authorId,
                                                       @RequestBody @Valid AuthorLayerUpdateDto updateDto) {
        Member member = memberFacade.loadMember(user);
        return Response.success(authorLayerFacade.update(member, authorId, updateDto));
    }

    @RequestMapping(value = "/authors/{authorId}/profile-image", method = RequestMethod.PUT)
    public Response<Map<String, String>> updateProfileImage(JwtUser user,
                                                            @PathVariable long authorId,
                                                            @RequestParam("profile-image") MultipartFile profileImage) {
        Member member = memberFacade.loadMember(user);
        String imageUrl = authorLayerFacade.updateProfileImage(member, authorId, profileImage);
        Map<String, String> result = new HashMap<>();
        result.put("profile_image_url", imageUrl);
        return Response.success(result);
    }

    @RequestMapping(value = "/authors/{authorId}/profile-image", method = RequestMethod.DELETE)
    public Response deleteProfileImage(JwtUser user, @PathVariable long authorId) {
        Member member = memberFacade.loadMember(user);
        authorLayerFacade.deleteProfileImage(member, authorId);
        return Response.success();
    }

    @RequestMapping(value = "/authors/{authorId}/papers/all", method = RequestMethod.GET)
    public Response<List<PaperTitleDto>> getAllPaperTitles(@PathVariable long authorId) {
        return Response.success(authorLayerFacade.getAllPaperTitles(authorId));
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Getter
    @Setter
    private static class PaperIdWrapper {
        private Set<Long> paperIds = new HashSet<>();

        public void setPaperIds(Set<Long> paperIds) {
            if (CollectionUtils.isEmpty(paperIds)) {
                return;
            }
            this.paperIds = paperIds
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
    }

}
