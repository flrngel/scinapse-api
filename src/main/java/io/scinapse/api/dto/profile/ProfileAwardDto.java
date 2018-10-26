package io.scinapse.api.dto.profile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import io.scinapse.api.model.profile.ProfileAward;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class ProfileAwardDto {

    private String id;
    private String profileId;

    @JsonSerialize(using = DateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
    @NotNull
    private Date receivedDate;

    @Size(min = 2, max = 200)
    @NotNull
    private String title;

    @Size(min = 2, max = 200)
    private String description;

    public ProfileAwardDto(ProfileAward award) {
        this.id = award.getId();
        this.profileId = award.getProfile().getId();
        this.receivedDate = award.getReceivedDate();
        this.title = award.getTitle();
        this.description = award.getDescription();
    }

    public ProfileAward toEntity() {
        ProfileAward award = new ProfileAward();
        award.setReceivedDate(this.receivedDate);
        award.setTitle(this.title);
        award.setDescription(this.description);
        return award;
    }

    public void setTitle(String title) {
        this.title = StringUtils.normalizeSpace(title);
    }

    public void setDescription(String description) {
        this.description = StringUtils.normalizeSpace(description);
    }

}
