package network.pluto.absolute.controller;

import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.CollectionDto;
import network.pluto.absolute.dto.CollectionPaperDto;
import network.pluto.absolute.dto.collection.CollectionPaperAddRequest;
import network.pluto.absolute.dto.collection.CollectionPaperUpdateRequest;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.facade.CollectionFacade;
import network.pluto.absolute.security.jwt.JwtUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public Map<String, Object> findByCreators(@PathVariable long memberId,
                                              @PageableDefault Pageable pageable) {
        Page<CollectionDto> dtos = collectionFacade.findByCreator(memberId, pageable);

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

    @RequestMapping(value = "/collections/papers", method = RequestMethod.POST)
    public Result addPaper(@ApiIgnore JwtUser user, @RequestBody @Valid CollectionPaperAddRequest request) {
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

    @RequestMapping(value = "/collections/{collectionId}/papers/{paperIds}", method = RequestMethod.DELETE)
    public Result deletePapers(@ApiIgnore JwtUser user, @PathVariable long collectionId, @PathVariable List<Long> paperIds) {
        collectionFacade.delete(user, collectionId, paperIds);
        return Result.success();
    }

}