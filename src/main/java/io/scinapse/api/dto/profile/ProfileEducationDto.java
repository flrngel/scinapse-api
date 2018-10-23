package io.scinapse.api.dto.profile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import io.scinapse.api.model.profile.ProfileEducation;
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
public class ProfileEducationDto {

    private String id;
    private String profileId;

    @JsonSerialize(using = DateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
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
    private String department;

    @Size(min = 2, max = 100)
    private String degree;

    public ProfileEducationDto(ProfileEducation education) {
        this.id = education.getId();
        this.profileId = education.getProfile().getId();
        this.startDate = education.getStartDate();
        this.endDate = education.getEndDate();
        this.current = education.isCurrent();
        this.institution = education.getInstitution();
        this.department = education.getDepartment();
        this.degree = education.getDegree();
    }

    public ProfileEducation toEntity() {
        ProfileEducation education = new ProfileEducation();
        education.setStartDate(this.startDate);
        education.setEndDate(this.endDate);
        education.setCurrent(this.current);
        education.setInstitution(this.institution);
        education.setDepartment(this.department);
        education.setDegree(this.degree);
        return education;
    }

    public void setInstitution(String institution) {
        this.institution = StringUtils.normalizeSpace(institution);
    }

    public void setDepartment(String department) {
        this.department = StringUtils.normalizeSpace(department);
    }

    public void setDegree(String degree) {
        this.degree = StringUtils.normalizeSpace(degree);
    }

}
