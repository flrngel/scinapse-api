package io.scinapse.api.dto.mag;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.validator.NoSpecialChars;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Size;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class AuthorLayerUpdateDto {

    @NoSpecialChars
    @Size(min = 1)
    private String name;

    @Email
    private String email;

    @Size(min = 1)
    private String bio;

    @URL
    private String webPage;

    private Long affiliationId;

    public void setName(String name) {
        this.name = StringUtils.normalizeSpace(name);
    }

    public void setEmail(String email) {
        this.email = StringUtils.normalizeSpace(email);
    }

    public void setWebPage(String webPage) {
        this.webPage = StringUtils.normalizeSpace(webPage);
    }

}
