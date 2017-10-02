package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import network.pluto.bibliotheca.models.Member;

@NoArgsConstructor
@Data
public class MemberDto {

    @ApiModelProperty(readOnly = true)
    private Long id;

    @ApiModelProperty(required = true)
    private String email;

    @ApiModelProperty(required = true)
    private String password;

    @ApiModelProperty(required = true)
    private String fullName;

    private String profileImage;
    private String organization;

    @ApiModelProperty(readOnly = true)
    private WalletDto wallet;

    public MemberDto(Member member) {
        this.id = member.getMemberId();
        this.email = member.getEmail();
        this.fullName = member.getFullName();
        this.profileImage = member.getProfileImage();
        this.organization = member.getOrganization();

        if (member.getWallet() != null) {
            this.wallet = new WalletDto(member.getWallet());

        }
    }

    public Member toEntity() {
        Member member = new Member();
        member.setEmail(this.email);
        member.setPassword(this.password);
        member.setFullName(this.fullName);
        member.setOrganization(this.organization);

        return member;
    }
}
