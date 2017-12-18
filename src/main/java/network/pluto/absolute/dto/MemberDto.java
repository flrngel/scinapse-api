package network.pluto.absolute.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import network.pluto.absolute.dto.oauth.OauthUserDto;
import network.pluto.absolute.validator.Update;
import network.pluto.bibliotheca.models.Member;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;

@NoArgsConstructor
@ToString
@Getter
@Setter
public class MemberDto {

    @ApiModelProperty(readOnly = true)
    private Long id;

    @ApiModelProperty(required = true)
    @Email
    @NotNull
    private String email;

    @ApiModelProperty(readOnly = true)
    private boolean emailVerified = false;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty
    private String password;

    @ApiModelProperty(required = true)
    @Size(max = 250, groups = { Default.class, Update.class })
    @NotNull(groups = { Default.class, Update.class })
    private String name;

    @Size(max = 250)
    @URL
    private String profileImage;

    @Size(max = 250, groups = { Default.class, Update.class })
    @NotNull(groups = { Default.class, Update.class })
    private String affiliation;

    @Size(max = 250)
    private String major;

    private long reputation;

    @ApiModelProperty(readOnly = true)
    private long articleCount;

    @ApiModelProperty(readOnly = true)
    private long reviewCount;

    @ApiModelProperty(readOnly = true)
    private long commentCount;

    private OauthUserDto oauth;


    public MemberDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.emailVerified = member.isEmailVerified();
        this.name = member.getName();
        this.profileImage = member.getProfileImage();
        this.affiliation = member.getAffiliation();
        this.major = member.getMajor();
        this.reputation = member.getReputation();
    }

    public Member toEntity() {
        Member member = new Member();
        member.setEmail(this.email);
        member.setPassword(this.password);
        member.setName(this.name);
        member.setProfileImage(this.profileImage);
        member.setAffiliation(this.affiliation);
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
