package io.scinapse.api.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@NoArgsConstructor
public class AggregationDto {

    public static final Integer ALL = null;

    public List<Year> years = new ArrayList<>();
    public List<ImpactFactor> impactFactors = new ArrayList<>();
    public List<Journal> journals = new ArrayList<>();
    public List<Fos> fosList = new ArrayList<>();
    public List<String> keywordList = new ArrayList<>();

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Year {
        public Integer year = ALL;
        public long docCount;
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class ImpactFactor {
        public Integer from = ALL;
        public Integer to = ALL;
        public long docCount;
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Journal {
        public long id;
        public String title;
        public Double impactFactor;
        public long docCount;

        @JsonGetter("impact_factor")
        public Double getImpactFactor() {
            return impactFactor;
        }

        @Deprecated
        @JsonGetter("impactFactor")
        public Double getOldField() {
            return impactFactor;
        }
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Fos {
        public long id;
        public String name;
        public int level;
        public long docCount;
    }

    @Deprecated
    @JsonGetter
    public boolean isAvailable() {
        return true;
    }

}
