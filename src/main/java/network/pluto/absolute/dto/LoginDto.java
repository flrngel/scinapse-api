package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LoginDto {

    @ApiModelProperty(readOnly = true)
    private boolean loggedIn;

    @ApiModelProperty(readOnly = true)
    private String token;

    @ApiModelProperty(readOnly = true)
    private MemberDto member;
}
