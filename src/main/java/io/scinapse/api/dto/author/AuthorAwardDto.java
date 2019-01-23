package io.scinapse.api.dto.author;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import io.scinapse.api.data.scinapse.model.author.AuthorAward;
import io.scinapse.api.validator.Website;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.util.Date;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class AuthorAwardDto {

    private String id;

    @JsonSerialize(using = DateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy")
    @Past
    @NotNull
    private Date receivedDate;

    @Size(min = 1, max = 200)
    @NotNull
    private String title;

    @Size(min = 1)
    private String description;

    @Website
    @Size(min = 1)
    private String relatedLink;

    public AuthorAwardDto(AuthorAward award) {
        this.id = award.getId();
        this.receivedDate = award.getReceivedDate();
        this.title = award.getTitle();
        this.description = award.getDescription();
        this.relatedLink = award.getRelatedLink();
    }

    public AuthorAward toEntity() {
        AuthorAward award = new AuthorAward();
        award.setReceivedDate(this.receivedDate);
        award.setTitle(this.title);
        award.setDescription(this.description);
        award.setRelatedLink(this.relatedLink);
        return award;
    }

    public void setTitle(String title) {
        this.title = StringUtils.normalizeSpace(title);
    }

    public void setDescription(String description) {
        this.description = StringUtils.normalizeSpace(description);
    }

    public void setRelatedLink(String relatedLink) {
        this.relatedLink = StringUtils.normalizeSpace(relatedLink);
    }

}
