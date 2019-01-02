package io.scinapse.api.dto.author;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import io.scinapse.api.data.scinapse.model.author.AuthorEducation;
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
public class AuthorEducationDto {

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
    private String degree;

    public AuthorEducationDto(AuthorEducation education) {
        this.id = education.getId();
        this.startDate = education.getStartDate();
        this.endDate = education.getEndDate();
        this.current = education.isCurrent();
        this.institutionId = education.getAffiliationId();
        this.institutionName = education.getAffiliationName();
        this.department = education.getDepartment();
        this.degree = education.getDegree();
    }

    public AuthorEducation toEntity() {
        AuthorEducation education = new AuthorEducation();
        education.setStartDate(this.startDate);
        education.setEndDate(this.endDate);
        education.setCurrent(this.current);
        education.setAffiliationId(this.institutionId);
        education.setAffiliationName(this.institutionName);
        education.setDepartment(this.department);
        education.setDegree(this.degree);
        return education;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = StringUtils.normalizeSpace(institutionName);
    }

    public void setDepartment(String department) {
        this.department = StringUtils.normalizeSpace(department);
    }

    public void setDegree(String degree) {
        this.degree = StringUtils.normalizeSpace(degree);
    }

}
