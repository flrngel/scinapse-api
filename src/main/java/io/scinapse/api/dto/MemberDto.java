package io.scinapse.api.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.api.configuration.ScinapseConstant;
import io.scinapse.api.data.scinapse.model.Member;
import io.scinapse.api.dto.oauth.OauthUserDto;
import io.scinapse.api.validator.NoSpecialChars;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("author_id")
    private Long authorId;

    @JsonProperty("affiliation_id")
    private Long affiliationId;

    @Size(min = 1, max = 250)
    @NotNull
    @JsonProperty("affiliation_name")
    private String affiliationName;

    private OauthUserDto oauth;

    public MemberDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.emailVerified = member.isEmailVerified();

        this.affiliationId = member.getAffiliationId();
        this.affiliationName = member.getAffiliationName();


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

        member.setFirstName(this.firstName);
        member.setLastName(this.lastName);

        member.setAffiliationId(this.affiliationId);
        member.setAffiliationName(this.affiliationName);

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

    public void setAffiliationName(String affiliationName) {
        this.affiliationName = StringUtils.normalizeSpace(affiliationName);
    }

    public void setAffiliation(String affiliation) {
        this.affiliationName = StringUtils.normalizeSpace(affiliation);
    }

    public String getAffiliation() {
        return this.affiliationName;
    }

    @JsonGetter("is_author_connected")
    public boolean authorConnected() {
        return this.authorId != null && this.authorId > 0;
    }

}
