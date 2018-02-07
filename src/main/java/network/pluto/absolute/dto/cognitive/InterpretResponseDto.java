package network.pluto.absolute.dto.cognitive;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class InterpretResponseDto {

    private String query;
    private List<Interpretation> interpretations = new ArrayList<>();

    @Getter
    @Setter
    public static class Interpretation {
        private Long logprob;
        private String parse;
        private List<Rule> rules = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Rule {
        private String name;
        private Output output;
    }

    @Getter
    @Setter
    public static class Output {
        private String type;
        private String value;
    }

    public String getRecommendQuery() {
        List<String> queries = getInterpretedQuery();
        if (queries.size() == 1 && queries.get(0).startsWith("Ti=")) {
            return queries.get(0);
        }

        String subQuery = filterKeywordMatchQuery(queries);
        if (!StringUtils.hasText(subQuery)) {
            return null;
        }
        return "OR(" + subQuery + ")";
    }

    private List<String> getInterpretedQuery() {
        return interpretations
                .stream()
                .flatMap(interpret -> interpret.getRules().stream())
                .filter(rule -> rule.getOutput() != null)
                .map(Rule::getOutput)
                .map(Output::getValue)
                .filter(StringUtils::hasText)
                .filter(this::filterValidKeyword)
                .collect(Collectors.toList());
    }

    private boolean filterValidKeyword(String value) {
        return (value.contains("Composite") && (value.contains("F.FN") || value.contains("A.AuN"))) // FOS, author match query
                || value.startsWith("W=") // keyword match query
                || value.startsWith("Ti="); // title match query
    }

    private String filterKeywordMatchQuery(List<String> queries) {
        return queries.stream()
                .filter(value -> !value.startsWith("Ti=")) // remove title match query
                .limit(3) // take top 3 interpretations
                .collect(Collectors.joining(","));
    }

}
