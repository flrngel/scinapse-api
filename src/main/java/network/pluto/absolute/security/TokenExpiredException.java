package network.pluto.absolute.security;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class TokenExpiredException extends AuthenticationException {

    private final String token;
    private final String reason;

    public TokenExpiredException(String message, String token, String reason) {
        super(message);
        this.token = token;
        this.reason = reason;
    }

}
