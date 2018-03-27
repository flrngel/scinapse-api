package network.pluto.absolute.util;

import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.util.StringUtils;

@Getter
@Setter
public class Query {

    private String text;
    private String doi;
    private QueryFilter filter = new QueryFilter();

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

        // search specific fields
        MultiMatchQueryBuilder stemmedFieldQuery = getMainQueryClause();

        MatchQueryBuilder titleQuery = QueryBuilders.matchQuery("title", text).boost(10);
        MatchQueryBuilder abstractQuery = QueryBuilders.matchQuery("abstract", text).boost(10);
        MatchQueryBuilder authorNameQuery = QueryBuilders.matchQuery("author.name", text).boost(3);
        MatchQueryBuilder authorAffiliationQuery = QueryBuilders.matchQuery("author.affiliation", text);
        MatchQueryBuilder fosQuery = QueryBuilders.matchQuery("fos.name", text).boost(5);
        MatchQueryBuilder journalQuery = QueryBuilders.matchQuery("journal.title", text);

        return QueryBuilders.boolQuery()
                .must(stemmedFieldQuery)
                .should(titleQuery)
                .should(abstractQuery)
                .should(authorNameQuery)
                .should(authorAffiliationQuery)
                .should(fosQuery)
                .should(journalQuery)
                .filter(filter.toFilerQuery())
                .filter(filter.toExtraFilterQuery());
    }

    public QueryBuilder toAggregationQuery() {
        // search specific fields
        MultiMatchQueryBuilder stemmedFieldQuery = getMainQueryClause();

        return QueryBuilders.boolQuery()
                .must(stemmedFieldQuery)
                .filter(filter.toFilerQuery());
    }

    private MultiMatchQueryBuilder getMainQueryClause() {
        return QueryBuilders.multiMatchQuery(text, "title.en_stemmed")
                .field("abstract.en_stemmed")
                .field("authors.name.en_stemmed")
                .field("fos.name.en_stemmed")
                .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
                .minimumShouldMatch("3<75%");
    }

    public QueryBuilder toJournalQuery() {
        return QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("journal.title.keyword", text))
                .filter(filter.toFilerQuery());
    }

    public boolean isDoi() {
        return StringUtils.hasText(doi);
    }

    private QueryBuilder toDoiSearchQuery() {
        return QueryBuilders.matchQuery("doi.keyword", getDoi());
    }

}
