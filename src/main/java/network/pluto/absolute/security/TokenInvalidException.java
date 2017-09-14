package network.pluto.absolute.security;

import lombok.Getter;

@Getter
public class TokenInvalidException extends RuntimeException {

    private final String reason;

    public TokenInvalidException(String message, String reason) {
        super(message);
        this.reason = reason;
    }
}
