package io.scinapse.api.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.api.configuration.ScinapseConstant;
import io.scinapse.api.data.scinapse.model.Member;
import io.scinapse.api.dto.oauth.OauthUserDto;
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
import java.util.Optional;

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
    @Size(min = 1, max = 50)
    @NotNull
    private String firstName;

    @NoSpecialChars
    @Size(min = 1, max = 50)
    @NotNull
    private String lastName;

    @Size(max = 250)
    @URL
    private String profileImage;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("author_id")
    private Long authorId;

    @Size(min = 1, max = 200, groups = { Default.class, Update.class })
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
        this.authorId = member.getAuthorId();

        Optional.ofNullable(member.getProfileImage())
                .ifPresent(key -> this.profileImageUrl = ScinapseConstant.SCINAPSE_MEDIA_URL + key);
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

    @JsonGetter("is_author_connected")
    public boolean authorConnected() {
        return this.authorId != null && this.authorId > 0;
    }

}
