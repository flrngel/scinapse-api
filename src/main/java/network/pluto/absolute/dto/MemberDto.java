package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import network.pluto.bibliotheca.models.Member;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Data
public class MemberDto {

    @ApiModelProperty(readOnly = true)
    private Long id;

    @ApiModelProperty(required = true)
    @Email
    @NotNull
    private String email;

    @ApiModelProperty(required = true)
    @Size(min = 8, message = "password must be greater than or equal to 8")
    @NotNull
    private String password;

    @ApiModelProperty(required = true)
    @NotNull
    private String fullName;

    private String profileImage;
    private String organization;
    private int reputation;

    @ApiModelProperty(readOnly = true)
    private WalletDto wallet;

    public MemberDto(Member member) {
        this.id = member.getMemberId();
        this.email = member.getEmail();
        this.fullName = member.getFullName();
        this.profileImage = member.getProfileImage();
        this.organization = member.getOrganization();
        this.reputation = member.getReputation();

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
