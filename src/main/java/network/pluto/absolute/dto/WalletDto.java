package network.pluto.absolute.dto;

import lombok.Data;
import network.pluto.bibliotheca.models.Wallet;

@Data
public class WalletDto {
    private String address;

    public static WalletDto fromEntity(Wallet wallet) {
        if (wallet == null) {
            return null;
        }

        WalletDto dto = new WalletDto();
        dto.setAddress(wallet.getAddress());
        return dto;
    }
}
