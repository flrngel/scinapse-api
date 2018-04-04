package network.pluto.absolute.util;

import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.rescore.QueryRescoreMode;
import org.elasticsearch.search.rescore.QueryRescorerBuilder;
import org.springframework.util.StringUtils;

@Getter
@Setter
public class Query {

    private String text;
    private String doi;
    private QueryFilter filter = new QueryFilter();
    private boolean journalSearch = false;

    private Query(String text) {
        if (StringUtils.hasText(text)) {
            this.text = text.trim();
            this.doi = TextUtils.parseDoi(this.text);
        }
    }

    public static Query parse(String queryStr) {
        return new Query(queryStr);
    }

    public static Query parse(String queryStr, String filterStr) {
        Query query = Query.parse(queryStr);
        query.setFilter(QueryFilter.parse(filterStr));
        return query;
    }

    public boolean isValid() {
        return StringUtils.hasText(text) && text.length() >= 2 && text.length() < 200;
    }

    public QueryBuilder toQuery() {
        if (isDoi()) {
            return toDoiSearchQuery();
        }

        if (journalSearch) {
            return toJournalQuery();
        }

        // search specific fields
        MultiMatchQueryBuilder stemmedFieldQuery = getMainQueryClause();

        MatchQueryBuilder titleQuery = QueryBuilders.matchQuery("title", text).boost(5);
        MatchQueryBuilder titleShingleQuery = QueryBuilders.matchQuery("title.shingles", text).boost(7);
        MatchQueryBuilder abstractQuery = QueryBuilders.matchQuery("abstract", text).boost(3);
        MatchQueryBuilder abstractShingleQuery = QueryBuilders.matchQuery("abstract.shingles", text).boost(5);
        MatchQueryBuilder authorNameQuery = QueryBuilders.matchQuery("author.name", text).boost(3);
        MatchQueryBuilder authorAffiliationQuery = QueryBuilders.matchQuery("author.affiliation", text);
        MatchQueryBuilder fosQuery = QueryBuilders.matchQuery("fos.name", text).boost(3);
        MatchQueryBuilder journalQuery = QueryBuilders.matchQuery("journal.title", text);

        return QueryBuilders.boolQuery()
                .must(stemmedFieldQuery)
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

    public MultiMatchQueryBuilder getMainQueryClause() {
        return QueryBuilders.multiMatchQuery(text, "title.en_stemmed")
                .field("abstract.en_stemmed")
                .field("authors.name.en_stemmed")
                .field("fos.name.en_stemmed")
                .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
                .minimumShouldMatch("3<75%");
    }

    public QueryRescorerBuilder getPhraseRescoreQuery() {
        MatchPhraseQueryBuilder titleQuery = QueryBuilders.matchPhraseQuery("title.en_stemmed", text).slop(5);
        MatchPhraseQueryBuilder abstractQuery = QueryBuilders.matchPhraseQuery("abstract.en_stemmed", text).slop(5);

        // phrase match booster for re-scoring
        BoolQueryBuilder phraseMatchQuery = QueryBuilders.boolQuery()
                .should(titleQuery)
                .should(abstractQuery);

        // re-scoring top 500 documents only
        return QueryRescorerBuilder.queryRescorer(phraseMatchQuery).windowSize(500);
    }

    public QueryRescorerBuilder getCitationRescoreQuery() {
        // citation count booster for re-scoring
        FieldValueFactorFunctionBuilder citationFunction = ScoreFunctionBuilders.fieldValueFactorFunction("citation_count").modifier(FieldValueFactorFunction.Modifier.LOG1P);
        FunctionScoreQueryBuilder citationBooster = QueryBuilders.functionScoreQuery(citationFunction).maxBoost(10); // limit boosting

        // re-scoring top 500 documents only
        return QueryRescorerBuilder.queryRescorer(citationBooster)
                .windowSize(500)
                .setScoreMode(QueryRescoreMode.Multiply);
    }

    public boolean shouldRescore() {
        return !isDoi() && !journalSearch;
    }

    public boolean isDoi() {
        return StringUtils.hasText(doi);
    }

    private QueryBuilder toDoiSearchQuery() {
        return QueryBuilders.matchQuery("doi.keyword", getDoi());
    }

    private QueryBuilder toJournalQuery() {
        return QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("journal.title.keyword", text))
                .filter(filter.toFilerQuery());
    }

}
