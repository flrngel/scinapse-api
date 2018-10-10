package io.scinapse.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.scinapse.api.dto.oauth.OauthUserDto;
import io.scinapse.api.model.Member;
import io.scinapse.api.validator.NoSpecialChars;
import io.scinapse.api.validator.Update;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;

@NoArgsConstructor
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

    @NoSpecialChars
    @Size(min = 2, max = 50)
    @NotNull
    private String firstName;

    @NoSpecialChars
    @Size(min = 2, max = 50)
    @NotNull
    private String lastName;

    @Size(max = 250)
    @URL
    private String profileImage;

    @Size(min = 2, max = 200, groups = { Default.class, Update.class })
    @NotNull(groups = { Default.class, Update.class })
    private String affiliation;

    @Size(max = 250)
    private String major;

    @ApiModelProperty(readOnly = true)
    private long commentCount;

    private OauthUserDto oauth;


    public MemberDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.emailVerified = member.isEmailVerified();
        this.profileImage = member.getProfileImage();
        this.affiliation = member.getAffiliation();
        this.major = member.getMajor();

        this.firstName = member.getFirstName();
        this.lastName = member.getLastName();
    }

    public Member toEntity() {
        Member member = new Member();
        member.setEmail(this.email);
        member.setPassword(this.password);
        member.setProfileImage(this.profileImage);
        member.setAffiliation(this.affiliation);
        member.setMajor(this.major);

        member.setFirstName(this.firstName);
        member.setLastName(this.lastName);
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

    public void setFirstName(String firstName) {
        this.firstName = StringUtils.normalizeSpace(firstName);
    }

    public void setLastName(String lastName) {
        this.lastName = StringUtils.normalizeSpace(lastName);
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = StringUtils.normalizeSpace(affiliation);
    }

}
