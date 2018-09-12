package io.scinapse.api.dto.mag;

import io.scinapse.api.model.mag.PaperFieldsOfStudy;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class FosDto {

    private long paperId;
    private long id;
    private String fos;
    private String name;

    public FosDto(PaperFieldsOfStudy fos) {
        this.paperId = fos.getPaper().getId();
        this.id = fos.getFieldsOfStudy().getId();
        this.fos = fos.getFieldsOfStudy().getName();
        this.name = fos.getFieldsOfStudy().getName();
    }

}
