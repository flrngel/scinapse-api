package io.scinapse.api.dto.oauth;

import io.scinapse.domain.enums.OauthVendor;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;

@Getter
@Setter
public class OAuthAuthorizeUriDto {

    private OauthVendor vendor;
    private URI uri;

}
