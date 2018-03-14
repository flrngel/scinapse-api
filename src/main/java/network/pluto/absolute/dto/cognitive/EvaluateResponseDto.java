package network.pluto.absolute.dto.cognitive;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import network.pluto.absolute.util.JsonUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
public class EvaluateResponseDto {

    @JsonProperty("expr")
    private String expression;

    @JsonProperty
    private List<Entity> entities = new ArrayList<>();

    @Getter
    @Setter
    public static class Entity {

        @JsonProperty("logprob")
        private double logProb;

        @JsonProperty("Id")
        private long cognitivePaperId;

        @JsonProperty("Ti")
        private String titleNormalized;

        @JsonProperty("DN")
        private String titleDisplay;

        @JsonProperty("VFN")
        private String journalName;

        // e.g) 2018
        @JsonProperty("Y")
        private int year;

        // e.g) 2018-01-01
        @JsonProperty("D")
        private String date;

        @JsonProperty("IA")
        private InvertedAbstract invertedAbstract;

        @JsonProperty("CC")
        private int citationCount;

        @JsonProperty("RId")
        private long[] references = {};

        @JsonProperty("AA")
        private List<Author> authors = new ArrayList<>();

        @JsonProperty("F")
        private List<FieldOfStudy> fosList = new ArrayList<>();

        @JsonProperty("S")
        private List<Source> sources = new ArrayList<>();

        @JsonProperty("E")
        private String extra;

        public String getDoi() {
            if (!StringUtils.hasText(extra)) {
                return null;
            }

            try {
                return JsonUtils.getValue(extra, "DOI", String.class);
            } catch (IOException e) {
                log.error("Error occurs while extracting DOI from extra information", e);
                return null;
            }
        }

    }

    @Getter
    @Setter
    public static class Author {

        @JsonProperty("DAuN")
        private String authorDisplayName;

        @JsonProperty("DAfN")
        private String affiliationDisplayName;

        @JsonProperty("S")
        private int order;

    }

    @Getter
    @Setter
    public static class Source {

        @JsonProperty("Ty")
        private String type;

        @JsonProperty("U")
        private String url;

        public boolean isPdf() {
            return SourceType.find(type) == SourceType.PDF;
        }
    }

    public enum SourceType {
        HTML("1"),
        TEXT("2"),
        PDF("3"),
        DOC("4"),
        PPT("5"),
        XLS("6"),
        PS("7");

        private String indicator;

        SourceType(String indicator) {
            this.indicator = indicator;
        }

        public static SourceType find(String type) {
            for (SourceType sourceType : SourceType.values()) {
                if (sourceType.indicator.equals(type)) {
                    return sourceType;
                }
            }
            return null;
        }
    }

    @Getter
    @Setter
    public static class FieldOfStudy {

        @JsonProperty("FN")
        private String fosDisplayName;

    }

    @Getter
    @Setter
    public static class InvertedAbstract {

        @JsonProperty("IndexLength")
        private int length;

        @JsonProperty("InvertedIndex")
        private LinkedMultiValueMap<String, Integer> invertedIndex;

        public String toAbstract() {
            if (length == 0 || invertedIndex == null) {
                return null;
            }

            String[] abstractArray = new String[length];
            invertedIndex.keySet().forEach(key -> {
                invertedIndex.get(key).forEach(index -> {
                    if (index >= length) {
                        return;
                    }
                    abstractArray[index] = key;
                });
            });

            return Joiner.on(" ").skipNulls().join(abstractArray).trim();
        }
    }

}
