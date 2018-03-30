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
        // prevent short keyword title matching
        if (interpretations.size() != 1) {
            return null;
        }

        List<String> queries = getInterpretedQuery();
        if (queries.isEmpty()) {
            return null;
        }

        return queries.get(0);
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
        return value.startsWith("Ti="); // title match query
    }

}
