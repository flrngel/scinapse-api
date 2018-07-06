package io.scinapse.api.dto.oauth;

import io.scinapse.api.enums.OauthVendor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class OauthUserDto {

    private OauthVendor vendor;
    private String uuid;
    private String oauthId;
    private Map<String, Object> userData;
    private boolean connected = false;

}
