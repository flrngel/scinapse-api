package network.pluto.absolute.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QueryFilter {

    private String raw;
    private Integer yearStart;
    private Integer yearEnd;
    private Integer ifStart;
    private Integer ifEnd;

    public static QueryFilter parse(String filterStr) {
        QueryFilter queryFilter = new QueryFilter();

        if (!StringUtils.hasText(filterStr)) {
            return queryFilter;
        }

        queryFilter.setRaw(filterStr);

        Map<String, String> filterMap = new HashMap<>();

        List<String> filters = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(filterStr);
        for (String filter : filters) {
            List<String> kv = Splitter.on("=").trimResults().omitEmptyStrings().splitToList(filter);
            if (kv.size() != 2) {
                continue;
            }
            filterMap.put(kv.get(0), kv.get(1));
        }

        String year = filterMap.get("year");
        if (StringUtils.hasText(year)) {
            List<String> years = Splitter.on(":").trimResults().splitToList(year);
            try {
                if (years.size() == 2) {
                    if (StringUtils.hasText(years.get(0))) {
                        queryFilter.setYearStart(Integer.parseInt(years.get(0)));
                    }
                    if (StringUtils.hasText(years.get(1))) {
                        queryFilter.setYearEnd(Integer.parseInt(years.get(1)));
                    }
                }
            } catch (NumberFormatException ignore) {
                // ignore
            }
        }

        String impactFactor = filterMap.get("if");
        if (StringUtils.hasText(impactFactor)) {
            List<String> ifs = Splitter.on(":").trimResults().splitToList(impactFactor);
            try {
                if (ifs.size() == 2) {
                    if (StringUtils.hasText(ifs.get(0))) {
                        queryFilter.setIfStart(Integer.parseInt(ifs.get(0)));
                    }
                    if (StringUtils.hasText(ifs.get(1))) {
                        queryFilter.setIfEnd(Integer.parseInt(ifs.get(1)));
                    }
                }
            } catch (NumberFormatException ignore) {
                // ignore
            }
        }

        return queryFilter;
    }

    public BoolQueryBuilder toFilerQuery() {
        BoolQueryBuilder filterQuery = QueryBuilders.boolQuery();

        if (yearStart != null) {
            filterQuery.must(QueryBuilders.rangeQuery("year").gte(yearStart));
        }

        if (yearEnd != null) {
            filterQuery.must(QueryBuilders.rangeQuery("year").lte(yearEnd));
        }

        return filterQuery;
    }

    public String toCognitiveFilterQuery() {
        List<String> filters = new ArrayList<>();

        if (yearStart != null && yearStart > 0) {
            filters.add("Y>=" + yearStart);
        }

        if (yearEnd != null && yearEnd > 0) {
            filters.add("Y<=" + yearEnd);
        }

        return Joiner.on(",").join(filters);
    }
}
