package io.scinapse.api.dto;

import io.scinapse.api.model.mag.Affiliation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AffiliationDto {

    private long id;
    private String name;

    public AffiliationDto(Affiliation affiliation) {
        this.id = affiliation.getId();
        this.name = affiliation.getDisplayName();
    }

}
