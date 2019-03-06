package io.scinapse.api.dto.v2;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.dto.AggregationDto;
import io.scinapse.api.dto.SuggestionDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class PaperSearchAdditional {
    private boolean resultModified = false;
    private SuggestionDto suggestion;
    private AggregationDto aggregation;
    private MatchedAuthor matchedAuthor;
    private List<MatchedEntity> matchedEntities = new ArrayList<>();
}
