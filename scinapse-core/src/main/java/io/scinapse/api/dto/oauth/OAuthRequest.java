package io.scinapse.api.dto.oauth;

import io.scinapse.domain.enums.OauthVendor;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class OAuthRequest {

    @NotNull
    private OauthVendor vendor;

    private String code;

    private String redirectUri;

    private String token;

}
