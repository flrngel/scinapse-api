package io.scinapse.api.controller;

import io.scinapse.api.dto.CitationTextDto;
import io.scinapse.api.dto.mag.PaperAuthorDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.dto.response.Error;
import io.scinapse.api.dto.response.Response;
import io.scinapse.domain.enums.CitationFormat;
import io.scinapse.api.facade.PaperFacade;
import io.scinapse.api.util.ErrorUtils;
import io.scinapse.api.util.HttpUtils;
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
    public Map<String, Object> related(@PathVariable long paperId,
                                       @RequestParam(value = "top-cited", required = false) boolean isTopCited) {
        List<PaperDto> papers;
        if (isTopCited) {
            papers = paperFacade.getRecommendedPapers(paperId);
        } else {
            papers = paperFacade.getRelatedPapers(paperId);
        }

        HashMap<String, Object> result = new HashMap<>();
        Meta meta = papers.isEmpty() ? Meta.unavailable() : Meta.available();
        result.put("meta", meta);
        result.put("data", papers);

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

    @RequestMapping(value = "/papers/{paperId}/authors", method = RequestMethod.GET)
    public Response<List<PaperAuthorDto>> getPaperAuthor(@PathVariable long paperId, PageRequest pageRequest) {
        return Response.success(paperFacade.getPaperAuthors(paperId, pageRequest));
    }

}