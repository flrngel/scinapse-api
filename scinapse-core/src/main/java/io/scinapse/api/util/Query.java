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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Query {

    private String text;
    private int tokenLength = 0;
    private List<String> phraseQueries = new ArrayList<>();
    private String doi;
    private QueryFilter filter = new QueryFilter();
    private boolean journalSearch = false;
    private long journalId;

    private Query(String text) {
        this.text = StringUtils.normalizeSpace(text);
        if (StringUtils.isBlank(this.text)) {
            return;
        }

        this.tokenLength = StringUtils.split(this.text).length;
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
        return StringUtils.isNotBlank(text) && text.length() >= 2 && text.length() < 400;
    }

    public BoolQueryBuilder toRelevanceQuery() {
        return toRelevanceQuery(getMainQueryClause());
    }

    public BoolQueryBuilder toSortQuery() {
        return QueryBuilders.boolQuery().must(getMainQueryClause());
    }

    private BoolQueryBuilder toRelevanceQuery(QueryBuilder mainQuery) {
        MatchQueryBuilder titleQuery = QueryBuilders.matchQuery("title", text).boost(4);
        MatchQueryBuilder titleShingleQuery = QueryBuilders.matchQuery("title.shingles", text).boost(5);
        MatchQueryBuilder abstractQuery = QueryBuilders.matchQuery("abstract", text).boost(3);
        MatchQueryBuilder abstractShingleQuery = QueryBuilders.matchQuery("abstract.shingles", text).boost(2.5f);
        MatchQueryBuilder authorNameQuery = QueryBuilders.matchQuery("authors.name", text).boost(4);
        MatchQueryBuilder authorAffiliationQuery = QueryBuilders.matchQuery("authors.affiliation.name", text).boost(1);
        MatchQueryBuilder fosQuery = QueryBuilders.matchQuery("fos.name", text).boost(2);
        MatchQueryBuilder journalQuery = QueryBuilders.matchQuery("journal.title", text).boost(4);
        MatchQueryBuilder conferenceQuery = QueryBuilders.matchQuery("conference.title", text).boost(4);

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
                .should(conferenceQuery);
    }

    public QueryBuilder toTitleQuery() {
        if (isDoi()) {
            return toDoiQuery();
        }

        MatchQueryBuilder titleQuery = QueryBuilders.matchQuery("title", text).boost(2);
        MatchQueryBuilder titleShingleQuery = QueryBuilders.matchQuery("title.shingles", text).boost(2);
        MatchQueryBuilder authorNameQuery = QueryBuilders.matchQuery("authors.name", text).boost(2);
        MatchQueryBuilder authorAffiliationQuery = QueryBuilders.matchQuery("authors.affiliation.name", text);
        MatchQueryBuilder journalQuery = QueryBuilders.matchQuery("journal.title", text);

        return QueryBuilders.boolQuery()
                .must(getMainQueryClause())
                .should(titleQuery)
                .should(titleShingleQuery)
                .should(authorNameQuery)
                .should(authorAffiliationQuery)
                .should(journalQuery);
    }

    public QueryBuilder getMainQueryClause() {
        BoolQueryBuilder query = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("base", text).minimumShouldMatch("3<-25% 7<-20%"));

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
            query.must(phrase);
        });

        return QueryBuilders.constantScoreQuery(query);
    }

    public QueryRescorerBuilder getTitlePhraseRescoreQuery() {
        MatchPhraseQueryBuilder titleQuery = QueryBuilders.matchPhraseQuery("title", text).slop(5).boost(7);

        // phrase match booster for re-scoring
        BoolQueryBuilder phraseMatchQuery = QueryBuilders.boolQuery()
                .should(titleQuery);

        // title boosting for phrase matching
        phraseQueries.forEach(q -> {
            MatchPhraseQueryBuilder phrase = QueryBuilders.matchPhraseQuery("title", q).boost(10);
            phraseMatchQuery.should(phrase);
        });

        // re-scoring top 50 documents only for each shard
        return new QueryRescorerBuilder(phraseMatchQuery)
                .windowSize(50)
                .setRescoreQueryWeight(7);
    }

    public QueryRescorerBuilder getAllTermRescoreQuery() {
        MultiMatchQueryBuilder rescoreQuery = QueryBuilders.multiMatchQuery(text, "journal.title")
                .field("title", 5)
                .field("abstract", 3)
                .field("authors.name", 1)
                .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
                .operator(Operator.AND);

        return new QueryRescorerBuilder(rescoreQuery)
                .windowSize(50)
                .setRescoreQueryWeight(5)
                .setScoreMode(QueryRescoreMode.Multiply);
    }

    public QueryRescorerBuilder getCitationRescoreQuery() {
        // citation count booster for re-scoring
        Script script = new Script("Math.log10(doc['citation_count'].value + 10)*1.285");
        ScriptScoreFunctionBuilder citationFunction = new ScriptScoreFunctionBuilder(script);
        FunctionScoreQueryBuilder.FilterFunctionBuilder citationBooster = new FunctionScoreQueryBuilder.FilterFunctionBuilder(citationFunction);


        FunctionScoreQueryBuilder functionQuery = QueryBuilders
                .functionScoreQuery(new FunctionScoreQueryBuilder.FilterFunctionBuilder[] { citationBooster })
                .maxBoost(4); // limit boosting

        // re-scoring top 50 documents only for each shard
        return new QueryRescorerBuilder(functionQuery)
                .windowSize(50)
                .setScoreMode(QueryRescoreMode.Multiply);
    }

    public QueryRescorerBuilder getHindexRescoreQuery() {
        Script script = new Script("Math.log10(doc['hindex.max'].value + 2.718)");
        ScriptScoreFunctionBuilder hindexFunction = new ScriptScoreFunctionBuilder(script);
        FunctionScoreQueryBuilder.FilterFunctionBuilder hindexBooster = new FunctionScoreQueryBuilder.FilterFunctionBuilder(hindexFunction);

        FunctionScoreQueryBuilder functionQuery = QueryBuilders
                .functionScoreQuery(new FunctionScoreQueryBuilder.FilterFunctionBuilder[] { hindexBooster })
                .maxBoost(4); // limit boosting

        // re-scoring top 50 documents only for each shard
        return new QueryRescorerBuilder(functionQuery)
                .windowSize(50)
                .setScoreMode(QueryRescoreMode.Multiply);

    }

    public QueryRescorerBuilder getImpactFactorRescoreQuery() {
        Script script = new Script("Math.log10(doc['journal.impact_factor'].value + 10)");
        ScriptScoreFunctionBuilder ifFunction = new ScriptScoreFunctionBuilder(script);
        FunctionScoreQueryBuilder.FilterFunctionBuilder ifBooster = new FunctionScoreQueryBuilder.FilterFunctionBuilder(ifFunction);


        FunctionScoreQueryBuilder functionQuery = QueryBuilders
                .functionScoreQuery(new FunctionScoreQueryBuilder.FilterFunctionBuilder[] { ifBooster })
                .maxBoost(3); // limit boosting

        // re-scoring top 50 documents only for each shard
        return new QueryRescorerBuilder(functionQuery)
                .windowSize(50)
                .setScoreMode(QueryRescoreMode.Multiply);
    }

    public QueryRescorerBuilder getConferenceTierRescoreQuery() {
        Script script = new Script("long tier = doc['conference.tier'].value; if (tier == 0) return 1; else return (5 - tier)*1.5;");
        ScriptScoreFunctionBuilder tierFunction = new ScriptScoreFunctionBuilder(script);
        FunctionScoreQueryBuilder.FilterFunctionBuilder tierBooster = new FunctionScoreQueryBuilder.FilterFunctionBuilder(tierFunction);


        FunctionScoreQueryBuilder functionQuery = QueryBuilders
                .functionScoreQuery(new FunctionScoreQueryBuilder.FilterFunctionBuilder[] { tierBooster })
                .maxBoost(6); // limit boosting

        // re-scoring top 50 documents only for each shard
        return new QueryRescorerBuilder(functionQuery)
                .windowSize(50)
                .setScoreMode(QueryRescoreMode.Multiply);
    }

    public QueryRescorerBuilder getYearRescoreQuery() {
        int currentYear = LocalDate.now().getYear();

        Map<String, Object> params = new HashMap<>();
        params.put("year", currentYear);

        String code = "long diff = params.year - doc['year'].value; if (diff < 4) return 3; else if (diff < 6) return 2.5; else if (diff < 10) return 1.5; else return 1;";
        Script script = new Script(Script.DEFAULT_SCRIPT_TYPE, Script.DEFAULT_SCRIPT_LANG, code, params);

        ScriptScoreFunctionBuilder yearFunction = new ScriptScoreFunctionBuilder(script);
        FunctionScoreQueryBuilder.FilterFunctionBuilder yearBooster = new FunctionScoreQueryBuilder.FilterFunctionBuilder(yearFunction);


        FunctionScoreQueryBuilder functionQuery = QueryBuilders
                .functionScoreQuery(new FunctionScoreQueryBuilder.FilterFunctionBuilder[] { yearBooster })
                .maxBoost(3); // limit boosting

        // re-scoring top 50 documents only for each shard
        return new QueryRescorerBuilder(functionQuery)
                .windowSize(50)
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

    public boolean isDoi() {
        return StringUtils.isNotBlank(doi);
    }

    public QueryBuilder toDoiQuery() {
        return QueryBuilders.matchQuery("doi", doi);
    }

}
