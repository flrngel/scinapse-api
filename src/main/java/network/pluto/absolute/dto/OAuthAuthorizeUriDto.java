package network.pluto.absolute.dto;

import lombok.Getter;
import lombok.Setter;
import network.pluto.absolute.enums.OauthVendor;

import java.net.URI;

@Getter
@Setter
public class OAuthAuthorizeUriDto {

    private OauthVendor vendor;
    private URI uri;

}
