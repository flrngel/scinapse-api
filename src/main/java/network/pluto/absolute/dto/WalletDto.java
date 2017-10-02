package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import network.pluto.bibliotheca.models.Wallet;

@NoArgsConstructor
@Data
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
