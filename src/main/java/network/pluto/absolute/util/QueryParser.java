package network.pluto.absolute.util;

import com.google.common.base.Splitter;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryParser {

    public static Query parse(String queryStr) {
        Map<String, String> queryMap = new HashMap<>();

        List<String> filters = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(queryStr);
        for (String filter : filters) {
            List<String> kv = Splitter.on("=").trimResults().omitEmptyStrings().splitToList(filter);
            if (kv.size() != 2) {
                continue;
            }
            queryMap.put(kv.get(0), kv.get(1));
        }

        String text = queryMap.get("text");
        Query query = new Query(text);

        String year = queryMap.get("year");
        if (StringUtils.hasText(year)) {
            List<String> years = Splitter.on(":").trimResults().splitToList(year);
            try {
                if (years.size() == 2) {
                    if (StringUtils.hasText(years.get(0))) {
                        query.setYearStart(Integer.parseInt(years.get(0)));
                    }
                    if (StringUtils.hasText(years.get(1))) {
                        query.setYearEnd(Integer.parseInt(years.get(1)));
                    }
                }
            } catch (NumberFormatException ignore) {
                // ignore
            }
        }

        return query;
    }
}
