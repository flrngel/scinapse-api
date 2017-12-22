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

    public Query(String text) {
        this.text = text;
    }

    public boolean isValid() {
        return StringUtils.hasText(text) && text.length() >= 2;
    }

    public QueryBuilder toQuery() {

        // search specific fields
        MultiMatchQueryBuilder query = QueryBuilders.multiMatchQuery(text, "title")
                .field("abstract", 3)
                .operator(Operator.AND)
                .analyzer("standard")
                .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
                .tieBreaker(0.5f);

        // bool query for bool filter
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery().must(query);

        if (yearStart != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("year").gte(yearStart));
        }

        if (yearEnd != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("year").lte(yearEnd));
        }

        // venue booster
        ExistsQueryBuilder venueExistsQuery = QueryBuilders.existsQuery("venue");
        WeightBuilder weight = new WeightBuilder().setWeight(2);
        FunctionScoreQueryBuilder.FilterFunctionBuilder function = new FunctionScoreQueryBuilder.FilterFunctionBuilder(venueExistsQuery, weight);

        return QueryBuilders.functionScoreQuery(
                boolQuery,
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[] { function });
    }
}
