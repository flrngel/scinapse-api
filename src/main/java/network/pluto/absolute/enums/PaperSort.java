package network.pluto.absolute.enums;

import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

public enum PaperSort {

    RELEVANCE,
    MOST_CITATIONS,
    OLDEST_FIRST,
    NEWEST_FIRST;

    public static PaperSort find(String name) {
        for (PaperSort sort : values()) {
            if (sort.name().equals(name)) {
                return sort;
            }
        }
        return null;
    }

    public static SortBuilder toSortBuilder(PaperSort sort) {
        if (sort == null) {
            return null;
        }
        switch (sort) {
            case MOST_CITATIONS:
                return SortBuilders.fieldSort("citation_count").order(SortOrder.DESC);
            case OLDEST_FIRST:
                return SortBuilders.fieldSort("year").order(SortOrder.ASC);
            case NEWEST_FIRST:
                return SortBuilders.fieldSort("year").order(SortOrder.DESC);
            default:
                return null;
        }
    }

}
