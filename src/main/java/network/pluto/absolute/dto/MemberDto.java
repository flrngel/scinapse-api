package network.pluto.absolute.dto;

import lombok.Data;
import network.pluto.bibliotheca.models.Member;

@Data
public class MemberDto {
    private String email;
    private String password;
    private String fullName;
    private WalletDto wallet;

    public static Member toEntity(MemberDto dto) {
        if (dto == null) {
            return null;
        }

        Member member = new Member();
        member.setEmail(dto.getEmail());
        member.setPassword(dto.getPassword());
        member.setFullName(dto.getFullName());
        return member;
    }

    public static MemberDto fromEntity(Member member) {
        if (member == null) {
            return null;
        }

        MemberDto dto = new MemberDto();
        dto.setEmail(member.getEmail());
        dto.setFullName(member.getFullName());

        WalletDto walletDto = WalletDto.fromEntity(member.getWallet());
        dto.setWallet(walletDto);

        return dto;
    }
}
