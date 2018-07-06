package io.scinapse.api.util;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {

    private static Pattern DOI_HTTP_PATTERN = Pattern.compile("^(?:(?:http://|https://)?(?:.+)?doi.org/)(.+)$", Pattern.CASE_INSENSITIVE);
    private static Pattern DOI_DOT_PATTERN = Pattern.compile("^doi\\s*:\\s*(.+)$", Pattern.CASE_INSENSITIVE);
    private static Pattern DOI_PATTERN = Pattern.compile("^10.\\d{4,9}/[-._;()/:A-Z0-9]+$", Pattern.CASE_INSENSITIVE);
    private static Pattern DOI_EXTRA_PATTERN1 = Pattern.compile("^10.1002/[^\\s]+$", Pattern.CASE_INSENSITIVE);
    private static Pattern DOI_EXTRA_PATTERN2 = Pattern.compile("^10.\\d{4}/\\d+-\\d+X?(\\d+)\\d+<[\\d\\w]+:[\\d\\w]*>\\d+.\\d+.\\w+;\\d$", Pattern.CASE_INSENSITIVE);
    private static Pattern DOI_EXTRA_PATTERN3 = Pattern.compile("^10.1021/\\w\\w\\d++$", Pattern.CASE_INSENSITIVE);
    private static Pattern DOI_EXTRA_PATTERN4 = Pattern.compile("^10.1207/[\\w\\d]+&\\d+_\\d+$", Pattern.CASE_INSENSITIVE);
    private static Pattern QUERY_PHRASE_MATCH = Pattern.compile("(?<= \")(.*?)(?=\" )");

    public static String parseDoi(String doiStr) {
        if (!StringUtils.hasText(doiStr)) {
            return null;
        }

        String doi = doiStr;

        Matcher httpMatcher = DOI_HTTP_PATTERN.matcher(doi);
        if (httpMatcher.matches()) {
            doi = httpMatcher.group(1);
        }

        Matcher dotMatcher = DOI_DOT_PATTERN.matcher(doi);
        if (dotMatcher.matches()) {
            doi = dotMatcher.group(1);
        }

        boolean isDoiPattern = false;
        if (DOI_PATTERN.matcher(doi).matches()) {
            isDoiPattern = true;
        } else if (DOI_EXTRA_PATTERN1.matcher(doi).matches()) {
            isDoiPattern = true;
        } else if (DOI_EXTRA_PATTERN2.matcher(doi).matches()) {
            isDoiPattern = true;
        } else if (DOI_EXTRA_PATTERN3.matcher(doi).matches()) {
            isDoiPattern = true;
        } else if (DOI_EXTRA_PATTERN4.matcher(doi).matches()) {
            isDoiPattern = true;
        }

        if (!isDoiPattern) {
            return null;
        }

        return doi;
    }

    public static List<String> parsePhrase(String query) {
        if (!StringUtils.hasText(query)) {
            return new ArrayList<>();
        }

        // do this for matching very first & last phrase
        String phraseQuery = " " + query.trim() + " ";

        List<String> results = new ArrayList<>();
        Matcher matcher = QUERY_PHRASE_MATCH.matcher(phraseQuery);
        while (matcher.find()) {
            results.add(matcher.group());
        }

        return results;
    }

}
