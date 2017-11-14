package network.pluto.absolute.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import network.pluto.absolute.validator.Update;
import network.pluto.bibliotheca.models.Member;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;

@NoArgsConstructor
@ToString(exclude = { "wallet" })
@Getter
@Setter
public class MemberDto {

    @ApiModelProperty(readOnly = true)
    private Long id;

    @ApiModelProperty(required = true)
    @Email
    @NotNull
    private String email;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty(required = true)
    @Size(min = 8, max = 50, message = "password length must be between 8 and 50")
    @NotNull
    private String password;

    @ApiModelProperty(required = true)
    @Size(max = 250)
    @NotNull(groups = { Default.class, Update.class })
    private String name;

    @Size(max = 250)
    @URL(groups = { Default.class, Update.class })
    private String profileImage;

    @Size(max = 250)
    @NotNull(groups = Update.class)
    private String institution;

    @Size(max = 250)
    private String major;

    private long reputation;

    @ApiModelProperty(readOnly = true)
    private WalletDto wallet;

    @ApiModelProperty(readOnly = true)
    private long articleCount;

    @ApiModelProperty(readOnly = true)
    private long reviewCount;

    @ApiModelProperty(readOnly = true)
    private long commentCount;

    public MemberDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.name = member.getName();
        this.profileImage = member.getProfileImage();
        this.institution = member.getInstitution();
        this.major = member.getMajor();
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
        member.setMajor(this.major);
        return member;
    }

    @Getter
    @Setter
    public static class PasswordWrapper {
        @ApiModelProperty(required = true)
        @Size(min = 8, message = "password must be greater than or equal to 8")
        @NotNull
        private String password;
    }
}
