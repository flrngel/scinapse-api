package network.pluto.absolute.dto;

import lombok.Getter;
import lombok.Setter;
import network.pluto.absolute.enums.OAuthVendor;

import java.net.URI;

@Getter
@Setter
public class OAuthAuthorizeUriDto {
    private OAuthVendor vendor;
    private URI uri;
}
