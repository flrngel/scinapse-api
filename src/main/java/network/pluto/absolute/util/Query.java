package network.pluto.absolute.util;

import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.WeightBuilder;
import org.springframework.util.StringUtils;

@Getter
@Setter
public class Query {
    private String text;
    private Integer yearStart;
    private Integer yearEnd;
    private Integer ifStart;
    private Integer ifEnd;

    public Query(String text) {
        this.text = text;
    }

    public boolean isValid() {
        return StringUtils.hasText(text) && text.length() >= 2;
    }

    public QueryBuilder toQuery() {

        // search specific fields
        MultiMatchQueryBuilder query = QueryBuilders.multiMatchQuery(text, "title")
                .field("title.en_stemmed")
                .field("abstract", 3)
                .field("abstract.en_stemmed", 3)
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
                .minimumShouldMatch("3<75%");

        // bool query for bool filter
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery().must(query);

        if (yearStart != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("year").gte(yearStart));
        }

        if (yearEnd != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("year").lte(yearEnd));
        }

        if (ifStart != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("journal.impact_factor").gte(ifStart));
        }

        if (ifEnd != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("journal.impact_factor").lte(ifEnd));
        }

        // journal booster
        ExistsQueryBuilder journalExistsQuery = QueryBuilders.existsQuery("journal.full_title");
        FunctionScoreQueryBuilder.FilterFunctionBuilder journalBooster = new FunctionScoreQueryBuilder.FilterFunctionBuilder(journalExistsQuery, new WeightBuilder().setWeight(3));

        // venue booster
        ExistsQueryBuilder venueExistsQuery = QueryBuilders.existsQuery("venue");
        FunctionScoreQueryBuilder.FilterFunctionBuilder venueBooster = new FunctionScoreQueryBuilder.FilterFunctionBuilder(venueExistsQuery, new WeightBuilder().setWeight(2));

        return QueryBuilders.functionScoreQuery(
                boolQuery,
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[] { journalBooster, venueBooster })
                .maxBoost(3); // limit boosting
    }
}
