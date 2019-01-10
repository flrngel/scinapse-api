package io.scinapse.api.dto.author;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import io.scinapse.api.data.scinapse.model.author.AuthorExperience;
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
public class AuthorExperienceDto {

    private String id;

    @JsonSerialize(using = DateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
    @Past
    @NotNull
    private Date startDate;

    @JsonSerialize(using = DateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
    private Date endDate;

    @JsonProperty("is_current")
    private boolean current;

    private Long institutionId;

    @Size(min = 1, max = 250)
    @NotNull
    private String institutionName;

    @Size(min = 1, max = 200)
    @NotNull
    private String department;

    @Size(min = 1, max = 100)
    @NotNull
    private String position;

    @Size(min = 1)
    private String description;

    public AuthorExperienceDto(AuthorExperience experience) {
        this.id = experience.getId();
        this.startDate = experience.getStartDate();
        this.endDate = experience.getEndDate();
        this.current = experience.isCurrent();
        this.institutionId = experience.getAffiliationId();
        this.institutionName = experience.getAffiliationName();
        this.department = experience.getDepartment();
        this.position = experience.getPosition();
        this.description = experience.getDescription();
    }

    public AuthorExperience toEntity() {
        AuthorExperience experience = new AuthorExperience();
        experience.setStartDate(this.startDate);
        experience.setEndDate(this.current ? null : this.endDate);
        experience.setCurrent(this.current);

        experience.setAffiliationId(this.institutionId);
        experience.setAffiliationName(this.institutionName);
        experience.setDepartment(this.department);
        experience.setPosition(this.position);
        experience.setDescription(this.description);
        return experience;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = StringUtils.normalizeSpace(institutionName);
    }

    public void setDepartment(String department) {
        this.department = StringUtils.normalizeSpace(department);
    }

    public void setPosition(String position) {
        this.position = StringUtils.normalizeSpace(position);
    }

}
