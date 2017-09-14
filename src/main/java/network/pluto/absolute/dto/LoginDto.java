package network.pluto.absolute.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor(staticName = "of")
@Data
public class LoginDto {
    private boolean loggedIn;
    private MemberDto member;
}
