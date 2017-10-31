package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import network.pluto.bibliotheca.models.Wallet;

@NoArgsConstructor
@ToString
@Getter
@Setter
public class WalletDto {

    @ApiModelProperty(readOnly = true)
    private Long id;

    @ApiModelProperty(readOnly = true)
    private String address;

    public WalletDto(Wallet wallet) {
        this.id = wallet.getWalletId();
        this.address = wallet.getAddress();
    }
}
