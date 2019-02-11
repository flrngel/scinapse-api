package io.scinapse.api.controller;

import io.scinapse.api.dto.response.Response;
import io.scinapse.api.dto.v2.AuthorItemDto;
import io.scinapse.api.dto.v2.EsPaperSearchResponse;
import io.scinapse.api.dto.v2.PaperItemDto;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.facade.SearchFacade;
import io.scinapse.api.util.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchFacade searchFacade;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public Response<List<PaperItemDto>> search(@RequestParam("q") String queryStr,
                                               @RequestParam(value = "filter", required = false) String filterStr,
                                               PageRequest pageRequest) {
        Query query = Query.parse(queryStr, filterStr);
        if (!query.isValid()) {
            throw new BadRequestException("Invalid query: too short or long query text. Query text length: " + query.getText());
        }

        EsPaperSearchResponse response = searchFacade.search(query, pageRequest);
        return Response.success(response.getPaperItemPage(), response.getAdditional());
    }

    @RequestMapping(value = "/search/authors", method = RequestMethod.GET)
    public Response<List<AuthorItemDto>> searchAuthors(@RequestParam("q") @Valid String queryStr, PageRequest pageRequest) {
        Query query = Query.parse(queryStr);
        if (!query.isValid()) {
            throw new BadRequestException("Invalid query: too short or long query text. Query text length: " + query.getText());
        }

        return Response.success(searchFacade.searchAuthors(query, pageRequest));
    }

}
