package io.scinapse.api.academic.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.data.academic.PaperFieldsOfStudy;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class AcPaperFosDto {

    private long id;
    private String name;

    public AcPaperFosDto(PaperFieldsOfStudy fos) {
        this.id = fos.getFieldsOfStudy().getId();
        this.name = fos.getFieldsOfStudy().getName();
    }

}
