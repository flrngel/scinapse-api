package network.pluto.absolute.dto.oauth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.absolute.enums.OauthVendor;

import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class OauthUserDto {

    private OauthVendor vendor;
    private String uuid;
    private String oauthId;
    private Map<String, Object> userData;
}
