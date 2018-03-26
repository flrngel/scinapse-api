package network.pluto.absolute.util;

import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class Query {

    private static Pattern DOI_HTTP_PATTERN = Pattern.compile("^(?:(?:http://|https://)?(?:.+)?doi.org/)(.+)$", Pattern.CASE_INSENSITIVE);
    private static Pattern DOI_DOT_PATTERN = Pattern.compile("^\\s*doi\\s*:\\s*(.+)$", Pattern.CASE_INSENSITIVE);
    private static Pattern DOI_PATTERN = Pattern.compile("^10.\\d{4,9}/[-._;()/:A-Z0-9]+$", Pattern.CASE_INSENSITIVE);
    private static Pattern DOI_EXTRA_PATTERN1 = Pattern.compile("^10.1002/[^\\s]+$", Pattern.CASE_INSENSITIVE);
    private static Pattern DOI_EXTRA_PATTERN2 = Pattern.compile("^10.\\d{4}/\\d+-\\d+X?(\\d+)\\d+<[\\d\\w]+:[\\d\\w]*>\\d+.\\d+.\\w+;\\d$", Pattern.CASE_INSENSITIVE);
    private static Pattern DOI_EXTRA_PATTERN3 = Pattern.compile("^10.1021/\\w\\w\\d++$", Pattern.CASE_INSENSITIVE);
    private static Pattern DOI_EXTRA_PATTERN4 = Pattern.compile("^10.1207/[\\w\\d]+&\\d+_\\d+$", Pattern.CASE_INSENSITIVE);

    private String text;
    private String doi;
    private QueryFilter filter = new QueryFilter();

    private Query(String text) {
        if (StringUtils.hasText(text)) {
            this.text = text.trim();
            parseDoi();
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

    private void parseDoi() {
        String query = this.text;

        Matcher httpMatcher = DOI_HTTP_PATTERN.matcher(query);
        if (httpMatcher.matches()) {
            query = httpMatcher.group(1);
        }

        Matcher dotMatcher = DOI_DOT_PATTERN.matcher(query);
        if (dotMatcher.matches()) {
            query = dotMatcher.group(1);
        }

        boolean isDoiPattern = false;
        if (DOI_PATTERN.matcher(query).matches()) {
            isDoiPattern = true;
        } else if (DOI_EXTRA_PATTERN1.matcher(query).matches()) {
            isDoiPattern = true;
        } else if (DOI_EXTRA_PATTERN2.matcher(query).matches()) {
            isDoiPattern = true;
        } else if (DOI_EXTRA_PATTERN3.matcher(query).matches()) {
            isDoiPattern = true;
        } else if (DOI_EXTRA_PATTERN4.matcher(query).matches()) {
            isDoiPattern = true;
        }

        if (isDoiPattern) {
            this.doi = query;
        }
    }

    public boolean isValid() {
        return StringUtils.hasText(text) && text.length() >= 2 && text.length() < 200;
    }

    public QueryBuilder toQuery() {
        if (isDoi()) {
            return toDoiSearchQuery();
        }

        // search specific fields
        MultiMatchQueryBuilder stemmedFieldQuery = QueryBuilders.multiMatchQuery(text, "title.en_stemmed")
                .field("abstract.en_stemmed")
                .field("authors.name.en_stemmed")
                .field("authors.affiliation.en_stemmed")
                .field("fos.name.en_stemmed")
                .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
                .minimumShouldMatch("3<75%");
//                .cutoffFrequency(0.01f); // turn off cutoff frequency temporary
//                .fuzziness(Fuzziness.AUTO); // turn off fuzziness temporary to improve precision

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
        MultiMatchQueryBuilder stemmedFieldQuery = QueryBuilders.multiMatchQuery(text, "title.en_stemmed")
                .field("abstract.en_stemmed")
                .field("authors.name.en_stemmed")
                .field("authors.affiliation.en_stemmed")
                .field("fos.name.en_stemmed")
                .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
                .minimumShouldMatch("3<75%");

        return QueryBuilders.boolQuery()
                .must(stemmedFieldQuery)
                .filter(filter.toFilerQuery());
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
