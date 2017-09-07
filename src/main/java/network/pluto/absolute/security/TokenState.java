package network.pluto.absolute.security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenState {

    private String accessToken;
    private Long expiresIn;

    public TokenState(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }
}
