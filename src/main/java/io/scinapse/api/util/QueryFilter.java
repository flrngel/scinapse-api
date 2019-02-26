package io.scinapse.api.util;

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class QueryFilter {

    private String raw;
    private Integer yearStart;
    private Integer yearEnd;
    private List<Long> journals = new ArrayList<>();
    private List<Long> fosList = new ArrayList<>();

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

        String journalStr = filterMap.get("journal");
        if (StringUtils.hasText(journalStr)) {
            try {
                List<Long> journals = Splitter.on("|")
                        .trimResults()
                        .omitEmptyStrings()
                        .splitToList(journalStr)
                        .stream()
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                queryFilter.setJournals(journals);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        String fosStr = filterMap.get("fos");
        if (StringUtils.hasText(fosStr)) {
            try {
                List<Long> fosList = Splitter.on("|")
                        .trimResults()
                        .omitEmptyStrings()
                        .splitToList(fosStr)
                        .stream()
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                queryFilter.setFosList(fosList);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        return queryFilter;
    }

    public BoolQueryBuilder toFilerQuery() {
        BoolQueryBuilder filterQuery = QueryBuilders.boolQuery();

        applyYearFilter(filterQuery);
        applyJournalFilter(filterQuery);
        applyFosFilter(filterQuery);

        return filterQuery;
    }

    public boolean hasFilter(boolean includeYear) {
        boolean hasFilter = !CollectionUtils.isEmpty(journals) || !CollectionUtils.isEmpty(fosList);

        if (!includeYear) {
            return hasFilter;
        }

        return hasFilter
                || yearStart != null
                || yearEnd != null;
    }

    public BoolQueryBuilder toYearAggFilter() {
        return toAggregationFilter(true, false, false);
    }

    public BoolQueryBuilder toJournalAggFilter() {
        return toAggregationFilter(false, true, false);
    }

    public BoolQueryBuilder toFosAggFilter() {
        return toAggregationFilter(false, false, true);
    }

    private BoolQueryBuilder toAggregationFilter(boolean year, boolean journal, boolean fos) {
        BoolQueryBuilder filterQuery = QueryBuilders.boolQuery();

        if (!year) {
            applyYearFilter(filterQuery);
        }

        if (!journal) {
            applyJournalFilter(filterQuery);
        }

        if (!fos) {
            applyFosFilter(filterQuery);
        }

        return filterQuery;
    }

    private void applyYearFilter(BoolQueryBuilder filterQuery) {
        if (yearStart != null) {
            filterQuery.must(QueryBuilders.rangeQuery("year").gte(yearStart));
        }

        if (yearEnd != null) {
            filterQuery.must(QueryBuilders.rangeQuery("year").lte(yearEnd));
        }
    }

    private void applyJournalFilter(BoolQueryBuilder filterQuery) {
        if (!journals.isEmpty()) {
            filterQuery.must(QueryBuilders.termsQuery("journal.id", journals));
        }
    }

    private void applyFosFilter(BoolQueryBuilder filterQuery) {
        if (!fosList.isEmpty()) {
            filterQuery.must(QueryBuilders.termsQuery("fos.id", fosList));
        }
    }

}
