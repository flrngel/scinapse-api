package network.pluto.absolute.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AggregationDto {

    public static final Integer ALL = null;

    public boolean available = false;

    public List<Year> years = new ArrayList<>();

    @JsonProperty("impact_factors")
    public List<ImpactFactor> impactFactors = new ArrayList<>();

    public List<Journal> journals = new ArrayList<>();

    @JsonProperty("fos_list")
    public List<Fos> fosList = new ArrayList<>();

    public static AggregationDto available() {
        AggregationDto dto = new AggregationDto();
        dto.available = true;
        return dto;
    }

    public static AggregationDto unavailable() {
        return new AggregationDto();
    }

    public static class Year {
        public Integer year = ALL;
        @JsonProperty("doc_count")
        public long docCount;
    }

    public static class ImpactFactor {
        public Integer from = ALL;
        public Integer to = ALL;
        @JsonProperty("doc_count")
        public long docCount;
    }

    public static class Journal {
        public long id;
        public String title;
        @JsonProperty("doc_count")
        public long docCount;
    }

    public static class Fos {
        public long id;
        public String name;
        @JsonProperty("doc_count")
        public long docCount;
    }

}
