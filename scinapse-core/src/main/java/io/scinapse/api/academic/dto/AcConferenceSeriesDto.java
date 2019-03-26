package io.scinapse.api.academic.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.domain.data.academic.model.ConferenceSeries;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class AcConferenceSeriesDto {
    private long id;
    private String name;
    private long paperCount;
    private long citationCount;

    public AcConferenceSeriesDto(ConferenceSeries conferenceSeries) {
        this.id = conferenceSeries.getId();
        this.name = conferenceSeries.getName();
        this.paperCount = conferenceSeries.getPaperCount();
        this.citationCount = conferenceSeries.getCitationCount();
    }
}
