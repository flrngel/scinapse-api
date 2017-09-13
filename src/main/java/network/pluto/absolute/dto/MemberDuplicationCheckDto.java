package network.pluto.absolute.dto;

import lombok.Data;

@Data
public class MemberDuplicationCheckDto {
    private Boolean duplicated;
    private String email;
    private String message;
}
