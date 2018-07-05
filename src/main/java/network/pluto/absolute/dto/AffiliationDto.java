package network.pluto.absolute.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.absolute.model.mag.Affiliation;

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
