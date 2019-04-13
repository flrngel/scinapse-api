package io.scinapse.api.dto.v2;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.domain.enums.OauthVendor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class OAuthToken {

    @NotNull
    private OauthVendor vendor;

    @Size(min = 1)
    @NotNull
    private String token;

}
