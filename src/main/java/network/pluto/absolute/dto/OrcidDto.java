package network.pluto.absolute.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.bibliotheca.models.Orcid;

@NoArgsConstructor
@Getter
@Setter
public class OrcidDto {

    @JsonProperty("orcid")
    private String orcid;

    @JsonProperty("name")
    private String name;

    @JsonProperty("access_token")
    private String accessToken;

    public OrcidDto(Orcid orcid) {
        this.orcid = orcid.getOrcid();
        this.name = orcid.getName();
    }

    public Orcid toEntity() {
        Orcid orcid = new Orcid();
        orcid.setOrcid(this.orcid);
        orcid.setName(this.name);
        return orcid;
    }
}
