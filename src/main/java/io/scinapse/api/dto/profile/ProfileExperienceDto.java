package io.scinapse.api.dto.profile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import io.scinapse.api.model.profile.ProfileExperience;
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
public class ProfileExperienceDto {

    private String id;
    private String profileId;

    @JsonSerialize(using = DateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
    @NotNull
    private Date startDate;

    @JsonSerialize(using = DateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
    private Date endDate;

    @JsonProperty("is_current")
    private boolean current;

    @Size(min = 2, max = 200)
    @NotNull
    private String institution;

    @Size(min = 2, max = 100)
    @NotNull
    private String department;

    @Size(min = 2, max = 100)
    @NotNull
    private String position;

    public ProfileExperienceDto(ProfileExperience experience) {
        this.id = experience.getId();
        this.profileId = experience.getProfile().getId();
        this.startDate = experience.getStartDate();
        this.endDate = experience.getEndDate();
        this.current = experience.isCurrent();
        this.institution = experience.getInstitution();
        this.department = experience.getDepartment();
        this.position = experience.getPosition();
    }

    public ProfileExperience toEntity() {
        ProfileExperience experience = new ProfileExperience();
        experience.setStartDate(this.startDate);
        experience.setEndDate(this.endDate);
        experience.setCurrent(this.current);
        experience.setInstitution(this.institution);
        experience.setDepartment(this.department);
        experience.setPosition(this.position);
        return experience;
    }

    public void setInstitution(String institution) {
        this.institution = StringUtils.normalizeSpace(institution);
    }

    public void setDepartment(String department) {
        this.department = StringUtils.normalizeSpace(department);
    }

    public void setPosition(String position) {
        this.position = StringUtils.normalizeSpace(position);
    }

}
