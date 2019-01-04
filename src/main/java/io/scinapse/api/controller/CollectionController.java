package io.scinapse.api.controller;

import io.scinapse.api.dto.collection.CollectionDto;
import io.scinapse.api.dto.collection.CollectionPaperDto;
import io.scinapse.api.dto.collection.CollectionPaperUpdateRequest;
import io.scinapse.api.dto.collection.MyCollectionDto;
import io.scinapse.api.dto.response.Response;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.facade.CollectionFacade;
import io.scinapse.api.security.jwt.JwtUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionFacade collectionFacade;

    @RequestMapping(value = "/collections", method = RequestMethod.POST)
    public Map<String, Object> create(@ApiIgnore JwtUser user,
                                      @RequestBody @Valid CollectionDto request) {
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BadRequestException("Title must be not empty");
        }

        CollectionDto dto = collectionFacade.create(user, request);
        Map<String, Object> result = new HashMap<>();
        result.put("data", dto);

        return result;
    }

    @RequestMapping(value = "/collections/{collectionId}", method = RequestMethod.GET)
    public Map<String, Object> read(@PathVariable long collectionId) {
        CollectionDto dto = collectionFacade.find(collectionId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", dto);

        return result;
    }

    @RequestMapping(value = "/collections/{collectionId}", method = RequestMethod.PUT)
    public Map<String, Object> update(@ApiIgnore JwtUser user,
                                      @PathVariable long collectionId,
                                      @RequestBody @Valid CollectionDto updated) {
        if (!StringUtils.hasText(updated.getTitle())) {
            throw new BadRequestException("Title must be not empty");
        }

        CollectionDto dto = collectionFacade.update(user, collectionId, updated);

        Map<String, Object> result = new HashMap<>();
        result.put("data", dto);

        return result;
    }

    @RequestMapping(value = "/collections/{collectionId}", method = RequestMethod.DELETE)
    public Result delete(@ApiIgnore JwtUser user, @PathVariable long collectionId) {
        collectionFacade.delete(user, collectionId);
        return Result.success();
    }

    @RequestMapping(value = "/members/{memberId}/collections", method = RequestMethod.GET)
    public Map<String, Object> findByCreators(@PathVariable long memberId) {
        PageRequest pageRequest = new PageRequest(0, 50, null);
        Page<CollectionDto> dtos = collectionFacade.findByCreator(memberId, pageRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("data", dtos);

        return result;
    }

    @RequestMapping(value = "/members/me/collections", method = RequestMethod.GET)
    public Map<String, Object> myCollection(@ApiIgnore JwtUser user,
                                            @RequestParam(value = "paper_id", required = false) Long paperId) {
        PageRequest pageRequest = new PageRequest(0, 50, null);
        Page<MyCollectionDto> dtos = collectionFacade.findMyCollection(user, paperId, pageRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("data", dtos);

        return result;
    }

    @RequestMapping(value = "/collections/{collectionId}/papers", method = RequestMethod.GET)
    public Map<String, Object> getPapers(@PathVariable long collectionId) {
        List<CollectionPaperDto> dtos = collectionFacade.getPapers(collectionId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", dtos);

        return result;
    }

    @RequestMapping(value = "/collections/{collectionId}/papers", method = RequestMethod.GET, params = "page")
    public Response<List<CollectionPaperDto>> getPapers(@PathVariable long collectionId, PageRequest pageRequest) {
        return Response.success(collectionFacade.getPapers(collectionId, pageRequest));
    }

    @RequestMapping(value = "/collections/{collectionId}/papers", method = RequestMethod.POST)
    public Result addPaper(@ApiIgnore JwtUser user, @PathVariable long collectionId, @RequestBody @Valid CollectionPaperDto request) {
        request.setCollectionId(collectionId);
        collectionFacade.addPaper(user, request);
        return Result.success();
    }

    @RequestMapping(value = "/collections/{collectionId}/papers/{paperId}", method = RequestMethod.PUT)
    public Map<String, Object> update(@ApiIgnore JwtUser user, @PathVariable long collectionId, @PathVariable long paperId, @RequestBody CollectionPaperUpdateRequest request) {
        CollectionPaperDto dto = collectionFacade.updateCollectionPaperNote(user, collectionId, paperId, request.getNote());

        Map<String, Object> result = new HashMap<>();
        result.put("data", dto);

        return result;
    }

    @RequestMapping(value = "/collections/{collectionId}/papers", method = RequestMethod.DELETE)
    public Result deletePapers(@ApiIgnore JwtUser user, @PathVariable long collectionId, @RequestParam("paper_ids") List<Long> paperIds) {
        if (paperIds.isEmpty()) {
            throw new BadRequestException("must specify paper IDs");
        }
        collectionFacade.deletePapers(user, collectionId, paperIds);
        return Result.success();
    }

}
