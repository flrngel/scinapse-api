package network.pluto.absolute.dto;

import lombok.Data;
import network.pluto.bibliotheca.models.Member;

@Data
public class MemberDto {
    private String email;
    private String password;
    private String fullName;
    private String profileImage;
    private WalletDto wallet;

    public Member toEntity() {
        Member member = new Member();
        member.setEmail(this.email);
        member.setPassword(this.password);
        member.setFullName(this.fullName);

        return member;
    }

    public static MemberDto fromEntity(Member member) {
        if (member == null) {
            return null;
        }

        MemberDto dto = new MemberDto();
        dto.setEmail(member.getEmail());
        dto.setFullName(member.getFullName());
        dto.setProfileImage(member.getProfileImage());

        WalletDto walletDto = WalletDto.fromEntity(member.getWallet());
        dto.setWallet(walletDto);

        return dto;
    }
}
