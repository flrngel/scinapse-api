package network.pluto.absolute.security;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class LoginRequest {
    private String email;
    private String password;
}
