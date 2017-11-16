package network.pluto.absolute.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.bibliotheca.models.Orcid;

@NoArgsConstructor
@Getter
@Setter
public class OrcidDto {

    private String orcid;

    private String name;

    private String accessToken;

    public OrcidDto(Orcid orcid) {
        this(orcid, false);
    }

    public OrcidDto(Orcid orcid, boolean includeToken) {
        this.orcid = orcid.getOrcid();
        this.name = orcid.getName();

        if (includeToken) {
            this.accessToken = orcid.getAccessToken();
        }
    }

    public Orcid toEntity() {
        Orcid orcid = new Orcid();
        orcid.setOrcid(this.orcid);
        orcid.setName(this.name);
        orcid.setAccessToken(this.accessToken);
        return orcid;
    }

    @JsonSetter(value = "accessToken")
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @JsonSetter(value = "access_token")
    public void setAccess_Token(String accessToken) {
        this.accessToken = accessToken;
    }
}
