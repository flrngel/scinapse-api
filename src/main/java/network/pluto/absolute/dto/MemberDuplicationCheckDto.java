package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ToString
@Getter
@Setter
public class MemberDuplicationCheckDto {

    @ApiModelProperty(readOnly = true)
    private Boolean duplicated;

    @ApiModelProperty(required = true)
    @NotNull
    private String email;

    @ApiModelProperty(readOnly = true)
    private String message;
}
