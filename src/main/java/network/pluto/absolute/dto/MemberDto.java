package network.pluto.absolute.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty(required = true)
    @Size(min = 8, message = "password must be greater than or equal to 8")
    @NotNull
    private String password;

    @ApiModelProperty(required = true)
    @NotNull
    private String name;

    private String profileImage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String institution;

    private int reputation;

    @ApiModelProperty(readOnly = true)
    private WalletDto wallet;

    public MemberDto(Member member) {
        this.id = member.getMemberId();
        this.email = member.getEmail();
        this.name = member.getName();
        this.profileImage = member.getProfileImage();
        this.institution = member.getInstitution();
        this.reputation = member.getReputation();

        if (member.getWallet() != null) {
            this.wallet = new WalletDto(member.getWallet());
        }
    }

    public Member toEntity() {
        Member member = new Member();
        member.setEmail(this.email);
        member.setPassword(this.password);
        member.setName(this.name);
        member.setProfileImage(this.profileImage);
        member.setInstitution(this.institution);
        return member;
    }
}
