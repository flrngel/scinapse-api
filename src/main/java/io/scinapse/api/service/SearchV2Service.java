package io.scinapse.api.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.dto.AggregationDto;
import io.scinapse.api.dto.SuggestionDto;
import io.scinapse.api.dto.v2.EsPaperSearchResponse;
import io.scinapse.api.enums.PaperSort;
import io.scinapse.api.util.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScriptScoreFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.rescore.QueryRescoreMode;
import org.elasticsearch.search.rescore.QueryRescorerBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.suggest.SortBy;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.phrase.DirectCandidateGeneratorBuilder;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestionBuilder;
import org.elasticsearch.search.suggest.term.TermSuggestionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@XRayEnabled
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchV2Service {

    private final RestHighLevelClient restHighLevelClient;
    private final SearchAggregationService aggregationService;

    private static final String HIGHLIGHT_PRE_TAG = "<b>";
    private static final String HIGHLIGHT_POST_TAG = "</b>";

    @Value("${pluto.server.es.index}")
    private String paperIndex;

    @Value("${pluto.server.es.index.author}")
    private String authorIndex;

    public EsPaperSearchResponse search(Query query, PageRequest pageRequest, boolean searchAgain) {
        MultiSearchRequest request = new MultiSearchRequest()
                .add(generatePaperSearchRequest(query, pageRequest, searchAgain))
                .add(generateAuthorSearchRequest(query));

        try {
            MultiSearchResponse.Item[] responses = restHighLevelClient.multiSearch(request).getResponses();

            EsPaperSearchResponse response = new EsPaperSearchResponse(query, pageRequest, responses);
            convertSuggest(response);

            if (shouldSearchAgain(searchAgain, response)) {
                SuggestionDto suggestion = response.getAdditional().getSuggestion();

                Query modifiedQuery = Query.parse(suggestion.getSuggestion());
                modifiedQuery.setFilter(query.getFilter());

                // search again
                response = search(modifiedQuery, pageRequest, true);

                // mark result as modified
                response.getAdditional().setResultModified(true);
                response.getAdditional().setSuggestion(suggestion);
            }

            convertAggregation(response);
            return response;
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Elasticsearch exception", e);
        }
    }

    private boolean shouldSearchAgain(boolean searchAgain, EsPaperSearchResponse response) {
        SuggestionDto suggestion = response.getAdditional().getSuggestion();
        return !searchAgain
                && CollectionUtils.isEmpty(response.getEsPapers())
                && suggestion != null
                && StringUtils.isNotBlank(suggestion.getSuggestion());
    }

    public EsPaperSearchResponse searchDoi(Query query, PageRequest pageRequest) {
        SearchSourceBuilder source = SearchSourceBuilder.searchSource()
                .query(query.toDoiQuery())
                .fetchSource(false)
                .from(pageRequest.getOffset())
                .size(pageRequest.getSize());

        SearchRequest request = new SearchRequest(paperIndex).source(source);

        try {
            SearchResponse response = restHighLevelClient.search(request);
            return new EsPaperSearchResponse(query, response);
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch exception", e);
        }
    }

    private void convertAggregation(EsPaperSearchResponse response) {
        AggregationDto dto = aggregationService.convertAggregation(response.getPaperResponse().getAggregations());
        response.getAdditional().setAggregation(dto);

        enhanceAggregation(response);
    }

    private void enhanceAggregation(EsPaperSearchResponse response) {
        AggregationDto dto = response.getAdditional().getAggregation();

        SearchSourceBuilder source = aggregationService.enhanceAggregationQuery(dto, response.getQuery());
        SearchRequest request = new SearchRequest(paperIndex).source(source);

        try {
            SearchResponse aggsResponse = restHighLevelClient.search(request);
            aggregationService.enhanceAggregation(dto, aggsResponse.getAggregations());
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch exception", e);
        }
    }

    private SearchRequest generatePaperSearchRequest(Query query, PageRequest pageRequest, boolean searchAgain) {
        QueryBuilder searchQuery;

        SortBuilder sortBuilder = PaperSort.toSortBuilder(pageRequest.getSort());
        if (sortBuilder != null) {
            searchQuery = query.toSortQuery();
        } else {
            searchQuery = query.toRelevanceQuery();
        }

        SearchSourceBuilder source = SearchSourceBuilder.searchSource()
                // set query
                .query(searchQuery)

                // do not retrieve source
                .fetchSource(false)

                // apply pagination
                .from(pageRequest.getOffset())
                .size(pageRequest.getSize());

        // add filter
        BoolQueryBuilder filter = QueryBuilders.boolQuery()
                .must(query.getFilter().toFilerQuery())
                .must(query.getFilter().toExtraFilterQuery());
        source.postFilter(filter);

        if (sortBuilder != null) {
            // add sort if sort parameter exists.
            source.sort(sortBuilder);
        } else {
            // add rescorer to boost relevance score.
            source.addRescorer(query.getPhraseRescoreQuery());
            source.addRescorer(query.getCitationRescoreQuery());
            source.addRescorer(query.getYearRescoreQuery());
            source.addRescorer(query.getAbsenceRescoreQuery());
        }

        // add highlighter
        source.highlighter(generateHighlighter(query));

        if (!searchAgain) {
            // add suggest
            source.suggest(generateSuggest(query));
        }

        // add aggregations
        source.aggregation(aggregationService.generateYearAggregation(query));
        source.aggregation(aggregationService.generateIfAggregation(query));
        source.aggregation(aggregationService.generateSampleAggregation());

        return new SearchRequest(paperIndex).source(source);
    }

    private SuggestBuilder generateSuggest(Query query) {
        if (query.getText().split(" ").length == 1) {
            return generateTermSuggest(query); // single term query
        }

        DirectCandidateGeneratorBuilder candidate = new DirectCandidateGeneratorBuilder("title")
                .size(10)
                .maxInspections(10)
                .minDocFreq(1)
                .maxTermFreq(0.001f)
                .sort(SortBy.FREQUENCY.name())
                .suggestMode(TermSuggestionBuilder.SuggestMode.POPULAR.name());

        PhraseSuggestionBuilder phraseSuggest = SuggestBuilders.phraseSuggestion("title.shingles")
                .text(query.getText())
                .analyzer("standard")
                .addCandidateGenerator(candidate)
                .size(1)
                .maxErrors(2)
                .highlight(HIGHLIGHT_PRE_TAG, HIGHLIGHT_POST_TAG)
                .collateQuery("{\"match\": {\"title\": {\"query\": \"{{suggestion}}\", \"operator\": \"and\"}}}");

        return new SuggestBuilder()
                .addSuggestion("suggest", phraseSuggest);
    }

    private SuggestBuilder generateTermSuggest(Query query) {
        TermSuggestionBuilder termSuggest = SuggestBuilders.termSuggestion("title")
                .text(query.getText())
                .size(1)
                .minDocFreq(1)
                .maxTermFreq(30)
                .sort(SortBy.FREQUENCY)
                .suggestMode(TermSuggestionBuilder.SuggestMode.POPULAR);

        return new SuggestBuilder()
                .addSuggestion("suggest", termSuggest);
    }

    private void convertSuggest(EsPaperSearchResponse response) {
        if (!CollectionUtils.isEmpty(response.getAuthorIds())) {
            // there are matched authors. do not provide suggestion.
            return;
        }

        Suggest suggest = response.getPaperResponse().getSuggest();
        if (suggest == null) {
            // no suggestion component.
            return;
        }

        List<? extends Suggest.Suggestion.Entry.Option> options = suggest
                .getSuggestion("suggest")
                .getEntries()
                .get(0)
                .getOptions();

        if (options.size() == 0) {
            // no suggestion.
            return;
        }

        String suggestion = options.get(0).getText().string();
        String highlighted = Optional.ofNullable(options.get(0).getHighlighted())
                .map(Text::string)
                .orElseGet(() -> HIGHLIGHT_PRE_TAG + suggestion + HIGHLIGHT_POST_TAG); // term suggester does not provide highlight

        SuggestionDto dto = new SuggestionDto(response.getQuery().getText(), suggestion, highlighted);
        response.getAdditional().setSuggestion(dto);
    }

    private HighlightBuilder generateHighlighter(Query query) {
        MultiMatchQueryBuilder highlightQuery = QueryBuilders.multiMatchQuery(query.getText(), "title.stemmed", "abstract.stemmed");

        return new HighlightBuilder()
                .field("title.stemmed")
                .field("abstract.stemmed")
                .numOfFragments(0)
                .preTags(HIGHLIGHT_PRE_TAG)
                .postTags(HIGHLIGHT_POST_TAG)
                .highlightQuery(highlightQuery);
    }

    private SearchRequest generateAuthorSearchRequest(Query query) {
        String queryText = query.getText();

        BoolQueryBuilder authorSearchQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("name", queryText).operator(Operator.AND));

        SearchSourceBuilder source = SearchSourceBuilder.searchSource()
                .query(authorSearchQuery)
                .addRescorer(getAuthorSearchRescorer())
                .fetchSource(false)
                .size(5);

        return new SearchRequest(authorIndex).source(source);
    }


    private QueryRescorerBuilder getAuthorSearchRescorer() {
        // author paper/citation count booster script
        Script script = new Script("Math.log10(doc['paper_count'].value + doc['citation_count'].value + 10)");
        ScriptScoreFunctionBuilder boostFunction = new ScriptScoreFunctionBuilder(script);

        FunctionScoreQueryBuilder rescoreQuery = QueryBuilders
                .functionScoreQuery(boostFunction)
                .maxBoost(2); // limit boosting

        return new QueryRescorerBuilder(rescoreQuery)
                .windowSize(10)
                .setScoreMode(QueryRescoreMode.Multiply);
    }

}