package io.scinapse.api.dto.mag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.util.TextUtils;
import io.scinapse.api.validator.NoSpecialChars;
import io.scinapse.api.validator.Website;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class AuthorLayerUpdateDto {

    @NoSpecialChars
    @Size(min = 1)
    @NotNull
    private String name;

    @Email
    @Size(min = 1)
    @NotNull
    private String email;

    @JsonProperty("is_email_hidden")
    private boolean emailHidden = false;

    @Size(min = 1)
    private String bio;

    @Website
    @Size(min = 1)
    private String webPage;

    private Long affiliationId;

    @Size(min = 1)
    private String affiliationName;

    public void setName(String name) {
        this.name = TextUtils.normalize(name);
    }

    public void setEmail(String email) {
        this.email = TextUtils.normalize(email);
    }

    public void setWebPage(String webPage) {
        this.webPage = TextUtils.normalize(webPage);
    }

    public void setAffiliationName(String affiliationName) {
        this.affiliationName = TextUtils.normalize(affiliationName);
    }

}
