package io.scinapse.api.dto.mag;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class AuthorLayerUpdateDto {

    @Size(min = 1)
    private String name;

    @Size(min = 1)
    private String bio;

}
