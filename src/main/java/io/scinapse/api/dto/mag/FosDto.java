package io.scinapse.api.dto.mag;

import io.scinapse.api.model.mag.FieldsOfStudy;
import io.scinapse.api.model.profile.ProfileFos;
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

    public FosDto(ProfileFos fos) {
        this(fos.getFos());
    }

}
