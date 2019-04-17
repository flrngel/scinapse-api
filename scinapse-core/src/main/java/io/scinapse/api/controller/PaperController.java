package io.scinapse.api.controller;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.dto.CitationTextDto;
import io.scinapse.api.dto.mag.PaperAuthorDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.dto.response.Error;
import io.scinapse.api.dto.response.Response;
import io.scinapse.api.facade.PaperFacade;
import io.scinapse.api.security.jwt.JwtUser;
import io.scinapse.api.util.ErrorUtils;
import io.scinapse.api.util.HttpUtils;
import io.scinapse.api.validator.NoSpecialChars;
import io.scinapse.domain.enums.CitationFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Transactional
    @RequestMapping(value = "/papers/{paperId}/request", method = RequestMethod.POST)
    public Response requestPaper(@PathVariable long paperId, @ApiIgnore JwtUser user, @RequestBody PaperRequestWrapper request) {
        Long memberId = Optional.ofNullable(user)
                .map(JwtUser::getId)
                .orElse(null);

        paperFacade.requestPaper(paperId, request, memberId);
        return Response.success();
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Setter
    @Getter
    public static class PaperRequestWrapper {
        @Email
        @Size(min = 1)
        @NotNull
        private String email;

        @NoSpecialChars
        @Size(min = 1)
        private String name;

        private String message;
    }

}
