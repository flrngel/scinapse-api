package io.scinapse.api.controller;

import io.scinapse.api.dto.CitationTextDto;
import io.scinapse.api.dto.mag.AuthorSearchPaperDto;
import io.scinapse.api.dto.mag.PaperAuthorDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.dto.response.Error;
import io.scinapse.api.dto.response.Response;
import io.scinapse.api.enums.CitationFormat;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.facade.PaperFacade;
import io.scinapse.api.util.ErrorUtils;
import io.scinapse.api.util.HttpUtils;
import io.scinapse.api.util.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PaperController {

    private final PaperFacade paperFacade;

    @RequestMapping(value = "/papers/{paperId}", method = RequestMethod.GET)
    public PaperDto find(@PathVariable long paperId, HttpServletRequest request) {
        return paperFacade.find(paperId, HttpUtils.isBot(request));
    }

    @RequestMapping(value = "/papers", method = RequestMethod.GET, params = "check_author_included")
    public Response<List<AuthorSearchPaperDto>> searchAuthorPaper(@RequestParam("query") String queryStr,
                                                                  @RequestParam(value = "filter", required = false) String filterStr,
                                                                  @RequestParam("check_author_included") long authorId,
                                                                  PageRequest pageRequest) {
        Query query = Query.parse(queryStr, filterStr);
        if (!query.isValid()) {
            throw new BadRequestException("Invalid query: too short or long query text");
        }

        return Response.success(paperFacade.searchAuthorPaper(query, authorId, pageRequest));
    }

    @ExceptionHandler({ UnsatisfiedServletRequestParameterException.class })
    public ResponseEntity handleRequestParamException(HttpServletRequest request, Exception ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorUtils.logError(status, request, ex);

        Error error = Error.of(request.getRequestURI(), status, ex);
        return new ResponseEntity<>(Response.error(error), status);
    }

    @RequestMapping(value = "/papers/{paperId}/references", method = RequestMethod.GET)
    public Page<PaperDto> paperReferences(@PathVariable long paperId,
                                          PageRequest pageRequest) {
        return paperFacade.findReferences(paperId, pageRequest);
    }

    @RequestMapping(value = "/papers/{paperId}/cited", method = RequestMethod.GET)
    public Page<PaperDto> paperCited(@PathVariable long paperId,
                                     PageRequest pageRequest) {
        return paperFacade.findCited(paperId, pageRequest);
    }

    @RequestMapping(value = "/papers/{paperId}/citation", method = RequestMethod.GET)
    public Map<String, Object> citation(@PathVariable long paperId, @RequestParam CitationFormat format) {
        CitationTextDto dto = paperFacade.citation(paperId, format);

        Map<String, Object> result = new HashMap<>();
        result.put("data", dto);
        return result;
    }

    @RequestMapping(value = "/papers/{paperId}/related", method = RequestMethod.GET)
    public Map<String, Object> related(@PathVariable long paperId) {
        List<PaperDto> related = paperFacade.getRelatedPapers(paperId);

        HashMap<String, Object> result = new HashMap<>();
        Meta meta = related.isEmpty() ? Meta.unavailable() : Meta.available();
        result.put("meta", meta);
        result.put("data", related);

        return result;
    }

    @RequestMapping(value = "/papers/{paperId}/authors/{authorId}/related", method = RequestMethod.GET)
    public HashMap<String, Object> authorRelated(@PathVariable long paperId, @PathVariable long authorId) {
        List<PaperDto> related = paperFacade.getAuthorRelatedPapers(paperId, authorId);

        HashMap<String, Object> result = new HashMap<>();
        Meta meta = related.isEmpty() ? Meta.unavailable() : Meta.available();
        result.put("meta", meta);
        result.put("data", related);

        return result;
    }

    @RequestMapping(value = "/papers/{paperId}/reading-now", method = RequestMethod.GET)
    public Response<List<PaperDto>> readingNow(@PathVariable long paperId) {
        return Response.success(paperFacade.getReadingNow(paperId));
    }

    @RequestMapping(value = "/papers/{paperId}/authors", method = RequestMethod.GET)
    public Response<List<PaperAuthorDto>> getPaperAuthor(@PathVariable long paperId, PageRequest pageRequest) {
        return Response.success(paperFacade.getPaperAuthors(paperId, pageRequest));
    }

}
