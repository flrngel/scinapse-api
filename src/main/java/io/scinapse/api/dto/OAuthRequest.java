package io.scinapse.api.dto;

import io.scinapse.api.enums.OauthVendor;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class OAuthRequest {

    @ApiModelProperty(required = true)
    @NotNull
    private OauthVendor vendor;

    @ApiModelProperty(required = true)
    @NotNull
    private String code;

    @ApiModelProperty
    private String redirectUri;

}
