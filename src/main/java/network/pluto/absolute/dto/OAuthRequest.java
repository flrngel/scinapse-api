package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.absolute.enums.OauthVendor;

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
