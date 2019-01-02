package io.scinapse.api.util;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScriptScoreFunctionBuilder;
import org.elasticsearch.index.query.functionscore.WeightBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.rescore.QueryRescoreMode;
import org.elasticsearch.search.rescore.QueryRescorerBuilder;

import java.util.List;

@Getter
@Setter
public class Query {

    private String text;
    private List<String> phraseQueries;
    private String doi;
    private QueryFilter filter = new QueryFilter();
    private boolean journalSearch = false;
    private long journalId;

    private Query(String text) {
        this.text = StringUtils.normalizeSpace(text);
        this.phraseQueries = TextUtils.parsePhrase(this.text);
        this.doi = TextUtils.parseDoi(this.text);
    }

    public static Query parse(String queryStr) {
        return new Query(queryStr);
    }

    public static Query parse(String queryStr, String filterStr) {
        Query query = Query.parse(queryStr);
        query.setFilter(QueryFilter.parse(filterStr));
        return query;
    }

    public static Query journal(long journalId) {
        Query query = new Query(null);
        query.setJournalSearch(true);
        query.setJournalId(journalId);
        return query;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(text) && text.length() >= 2 && text.length() < 200;
    }

    public QueryBuilder toQuery() {
        if (isDoi()) {
            return toDoiSearchQuery();
        }

        // search specific fields
        return toQuery(getMainQueryClause());
    }

    private QueryBuilder toQuery(QueryBuilder mainQuery) {
        MatchQueryBuilder titleQuery = QueryBuilders.matchQuery("title", text).boost(5);
        MatchQueryBuilder titleShingleQuery = QueryBuilders.matchQuery("title.shingles", text).boost(7);
        MatchQueryBuilder abstractQuery = QueryBuilders.matchQuery("abstract", text).boost(3);
        MatchQueryBuilder abstractShingleQuery = QueryBuilders.matchQuery("abstract.shingles", text).boost(5);
        MatchQueryBuilder authorNameQuery = QueryBuilders.matchQuery("authors.name", text).boost(3);
        MatchQueryBuilder authorAffiliationQuery = QueryBuilders.matchQuery("authors.affiliation.name", text);
        MatchQueryBuilder fosQuery = QueryBuilders.matchQuery("fos.name", text).boost(3);
        MatchQueryBuilder journalQuery = QueryBuilders.matchQuery("journal.title", text);

        return QueryBuilders.boolQuery()
                .must(mainQuery)
                .should(titleQuery)
                .should(titleShingleQuery)
                .should(abstractQuery)
                .should(abstractShingleQuery)
                .should(authorNameQuery)
                .should(authorAffiliationQuery)
                .should(fosQuery)
                .should(journalQuery)
                .filter(filter.toFilerQuery())
                .filter(filter.toExtraFilterQuery());
    }

    public QueryBuilder toSortQuery() {
        if (journalSearch) {
            return toJournalQuery();
        }

        QueryBuilder mainQuery = getMainQueryClause();
        return QueryBuilders.boolQuery()
                .must(mainQuery)
                .filter(filter.toFilerQuery())
                .filter(filter.toExtraFilterQuery());
    }

    public QueryBuilder toTitleQuery() {
        if (isDoi()) {
            return toDoiSearchQuery();
        }

        MultiMatchQueryBuilder mainQuery1 = QueryBuilders.multiMatchQuery(text, "authors.name")
                .field("authors.affiliation.name")
                .field("journal.title")
                .field("title")
                .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
                .minimumShouldMatch("-25%");

        MultiMatchQueryBuilder mainQuery2 = QueryBuilders.multiMatchQuery(text, "title.stemmed")
                .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
                .minimumShouldMatch("-25%");

        BoolQueryBuilder mainQuery = QueryBuilders.boolQuery()
                .should(mainQuery1)
                .should(mainQuery2);

        MatchQueryBuilder titleQuery = QueryBuilders.matchQuery("title", text).boost(2);
        MatchQueryBuilder titleShingleQuery = QueryBuilders.matchQuery("title.shingles", text).boost(2);
        MatchQueryBuilder authorNameQuery = QueryBuilders.matchQuery("authors.name", text).boost(2);
        MatchQueryBuilder authorAffiliationQuery = QueryBuilders.matchQuery("authors.affiliation.name", text);
        MatchQueryBuilder journalQuery = QueryBuilders.matchQuery("journal.title", text);

        return QueryBuilders.boolQuery()
                .must(mainQuery)
                .should(titleQuery)
                .should(titleShingleQuery)
                .should(authorNameQuery)
                .should(authorAffiliationQuery)
                .should(journalQuery);
    }

    public QueryBuilder getMainQueryClause() {

        MultiMatchQueryBuilder mainQuery1 = QueryBuilders.multiMatchQuery(text, "fos.name") // initializing field cannot contain boost factor
                .field("title")
                .field("abstract")
                .field("authors.name")
                .field("authors.affiliation.name")
                .field("journal.title")
                .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
                .minimumShouldMatch("-25%"); // combining with minimum_should_match seems to have a bug.

        MultiMatchQueryBuilder mainQuery2 = QueryBuilders.multiMatchQuery(text, "title.stemmed", "abstract.stemmed")
                .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
                .minimumShouldMatch("-25%");

        BoolQueryBuilder query = QueryBuilders.boolQuery()
                .should(mainQuery1)
                .should(mainQuery2);

        phraseQueries.forEach(q -> {
            MultiMatchQueryBuilder phrase = QueryBuilders.multiMatchQuery(
                    q,
                    "title",
                    "abstract",
                    "authors.name",
                    "authors.affiliation.name",
                    "fos.name",
                    "journal.title")
                    .type(MultiMatchQueryBuilder.Type.PHRASE);
            query.filter(phrase);
        });

        return query;
    }

    public QueryRescorerBuilder getPhraseRescoreQuery() {
        MatchPhraseQueryBuilder titleQuery = QueryBuilders.matchPhraseQuery("title.stemmed", text).slop(5).boost(7);
        MatchPhraseQueryBuilder abstractQuery = QueryBuilders.matchPhraseQuery("abstract.stemmed", text).slop(5).boost(5);

        // phrase match booster for re-scoring
        BoolQueryBuilder phraseMatchQuery = QueryBuilders.boolQuery()
                .should(titleQuery)
                .should(abstractQuery);

        // title boosting for phrase matching
        phraseQueries.forEach(q -> {
            MatchPhraseQueryBuilder phrase = QueryBuilders.matchPhraseQuery("title", q).boost(10);
            phraseMatchQuery.should(phrase);
        });

        // re-scoring top 100 documents only for each shard
        return new QueryRescorerBuilder(phraseMatchQuery)
                .windowSize(100)
                .setRescoreQueryWeight(2);
    }

    public QueryRescorerBuilder getCitationRescoreQuery() {
        // citation count booster for re-scoring
        Script script = new Script("Math.log10(doc['citation_count'].value + 10)");
        ScriptScoreFunctionBuilder citationFunction = new ScriptScoreFunctionBuilder(script);
        FunctionScoreQueryBuilder.FilterFunctionBuilder citationBooster = new FunctionScoreQueryBuilder.FilterFunctionBuilder(citationFunction);


        FunctionScoreQueryBuilder functionQuery = QueryBuilders
                .functionScoreQuery(new FunctionScoreQueryBuilder.FilterFunctionBuilder[] { citationBooster })
                .maxBoost(3); // limit boosting

        // re-scoring top 100 documents only for each shard
        return new QueryRescorerBuilder(functionQuery)
                .windowSize(100)
                .setScoreMode(QueryRescoreMode.Multiply);
    }

    public QueryRescorerBuilder getAbsenceRescoreQuery() {
        // abstract absent booster for re-scoring
        BoolQueryBuilder abstractAbsentFilter = QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("abstract"));
        FunctionScoreQueryBuilder.FilterFunctionBuilder abstractAbsentBooster = new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                abstractAbsentFilter,
                new WeightBuilder().setWeight(0.7f));

        // journal title absent booster for re-scoring
        BoolQueryBuilder journalTitleAbsentFilter = QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("journal.title"));
        FunctionScoreQueryBuilder.FilterFunctionBuilder journalTitleAbsentBooster = new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                journalTitleAbsentFilter,
                new WeightBuilder().setWeight(0.7f));

        FunctionScoreQueryBuilder functionQuery = QueryBuilders
                .functionScoreQuery(new FunctionScoreQueryBuilder.FilterFunctionBuilder[] { abstractAbsentBooster, journalTitleAbsentBooster })
                .maxBoost(1); // limit boosting

        // re-scoring top 10 documents only for each shard
        return new QueryRescorerBuilder(functionQuery)
                .windowSize(10) // to remove those papers only from very first page
                .setScoreMode(QueryRescoreMode.Multiply);
    }

    public boolean shouldRescore() {
        return !isDoi();
    }

    public boolean isDoi() {
        return StringUtils.isNotBlank(doi);
    }

    private QueryBuilder toDoiSearchQuery() {
        return QueryBuilders.matchQuery("doi", doi);
    }

    private QueryBuilder toJournalQuery() {
        return QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("journal.id", journalId))
                .filter(filter.toFilerQuery());
    }

}
