package io.scinapse.api.dto.mag;

import io.scinapse.domain.data.academic.Affiliation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AffiliationDto {

    private Long id;
    private String name;

    public AffiliationDto(Affiliation affiliation) {
        this.id = affiliation.getId();
        this.name = affiliation.getName();
    }

}
