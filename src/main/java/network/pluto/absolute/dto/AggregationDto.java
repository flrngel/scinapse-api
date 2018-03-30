package network.pluto.absolute.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AggregationDto {

    public boolean available = false;
    public boolean cognitive = false;

    public List<Year> years;

    @JsonProperty("impact_factors")
    public List<ImpactFactor> impactFactors;

    public List<Journal> journals;

    @JsonProperty("fos_list")
    public List<Fos> fosList;

    public static AggregationDto available() {
        AggregationDto dto = new AggregationDto();
        dto.available = true;
        return dto;
    }

    public static AggregationDto unavailable() {
        return new AggregationDto();
    }

    public static class Year {
        public int year;
        @JsonProperty("doc_count")
        public long docCount;
    }

    public static class ImpactFactor {
        public int from;
        public Integer to;
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
