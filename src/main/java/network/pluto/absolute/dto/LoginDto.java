package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class LoginDto {

    @ApiModelProperty(readOnly = true)
    private boolean loggedIn;

    @ApiModelProperty(readOnly = true)
    private String token;

    @ApiModelProperty(readOnly = true)
    private MemberDto member;

    public LoginDto(boolean loggedIn, String token, MemberDto member) {
        this.loggedIn = loggedIn;
        this.token = token;
        this.member = member;
    }
}
