package io.scinapse.api.dto.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.domain.enums.OauthVendor;
import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class OAuthConnection {

    private OauthVendor vendor;
    private String oAuthId;

    private String email;
    private String firstName;
    private String lastName;

    @JsonProperty("is_connected")
    private boolean connected;

}
