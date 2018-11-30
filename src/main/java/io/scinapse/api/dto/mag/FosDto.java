package io.scinapse.api.dto.mag;

import io.scinapse.api.data.academic.FieldsOfStudy;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FosDto {

    private long id;
    private String name;

    public FosDto(FieldsOfStudy fos) {
        this.id = fos.getId();
        this.name = fos.getName();
    }

}
