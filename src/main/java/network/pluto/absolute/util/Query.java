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
    private QueryFilter filter = new QueryFilter();

    public static Query parse(String queryStr) {
        Query query = new Query();
        if (StringUtils.hasText(queryStr)) {
            query.setText(queryStr.trim());
        }
        return query;
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

        // search specific fields
        MultiMatchQueryBuilder query = QueryBuilders.multiMatchQuery(text, "title")
                .field("title.en_stemmed")
                .field("abstract", 3)
                .field("abstract.en_stemmed", 3)
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
                .minimumShouldMatch("3<75%")
                .cutoffFrequency(0.01f);
//                .fuzziness(Fuzziness.AUTO); // turn off fuzziness temporary to improve precision

        // bool query for bool filter
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery().must(query);
        filter.filter(boolQuery);

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
