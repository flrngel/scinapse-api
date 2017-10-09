package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MemberDuplicationCheckDto {

    @ApiModelProperty(readOnly = true)
    private Boolean duplicated;

    @ApiModelProperty(required = true)
    @NotNull
    private String email;

    @ApiModelProperty(readOnly = true)
    private String message;
}
