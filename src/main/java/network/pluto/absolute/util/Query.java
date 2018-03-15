package network.pluto.absolute.util;

import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.query.functionscore.WeightBuilder;
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
                .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
                .minimumShouldMatch("3<75%");
//                .cutoffFrequency(0.01f); // turn off cutoff frequency temporary
//                .fuzziness(Fuzziness.AUTO); // turn off fuzziness temporary to improve precision

        MultiMatchQueryBuilder standardFieldQuery = QueryBuilders.multiMatchQuery(text, "title")
                .field("abstract")
                .field("authors.name")
                .field("authors.affiliation")
                .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
                .minimumShouldMatch("3<75%");

        MatchQueryBuilder journalQuery = QueryBuilders.matchQuery("journal.title", text);

        // bool query for bool filter
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(stemmedFieldQuery)
                .should(standardFieldQuery)
                .should(journalQuery)
                .filter(filter.toFilerQuery());

        // journal booster
        ExistsQueryBuilder abstractExistsQuery = QueryBuilders.existsQuery("abstract");
        FunctionScoreQueryBuilder.FilterFunctionBuilder abstractBooster = new FunctionScoreQueryBuilder.FilterFunctionBuilder(abstractExistsQuery, new WeightBuilder().setWeight(2));

        ExistsQueryBuilder journalExistsQuery = QueryBuilders.existsQuery("journal.title");
        FunctionScoreQueryBuilder.FilterFunctionBuilder journalBooster = new FunctionScoreQueryBuilder.FilterFunctionBuilder(journalExistsQuery, new WeightBuilder().setWeight(1.5f));

        FieldValueFactorFunctionBuilder citationFunction = ScoreFunctionBuilders.fieldValueFactorFunction("citation_count").modifier(FieldValueFactorFunction.Modifier.LOG1P);
        FunctionScoreQueryBuilder.FilterFunctionBuilder citationBooster = new FunctionScoreQueryBuilder.FilterFunctionBuilder(citationFunction);

        return QueryBuilders.functionScoreQuery(
                boolQuery,
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[] { abstractBooster, journalBooster, citationBooster })
                .maxBoost(10); // limit boosting
    }

    public QueryBuilder toJournalQuery() {
        return QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("journal.title", text))
                .filter(filter.toFilerQuery());
    }

    public boolean isDoi() {
        return StringUtils.hasText(doi);
    }

    private QueryBuilder toDoiSearchQuery() {
        return QueryBuilders.matchQuery("doi.raw", getDoi());
    }

}
