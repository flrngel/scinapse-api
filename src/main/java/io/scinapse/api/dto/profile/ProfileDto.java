package io.scinapse.api.dto.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.dto.MemberDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.model.profile.Profile;
import io.scinapse.api.validator.NoSpecialChars;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Email;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class ProfileDto {

    private String id;

    @NoSpecialChars
    @Size(min = 1, max = 50)
    @NotNull
    private String firstName;

    @NoSpecialChars
    @Size(min = 1, max = 50)
    @NotNull
    private String lastName;

    @Size(min = 1, max = 200)
    @NotNull
    private String affiliation;

    @Email
    private String email;

    private MemberDto member;

    @JsonProperty("is_mine")
    private boolean mine = false;

    @Size(min = 1, max = 20)
    @NotNull
    private List<Long> authorIds;

    private List<ProfileEducationDto> educations = new ArrayList<>();
    private List<ProfileExperienceDto> experiences = new ArrayList<>();
    private List<ProfileAwardDto> awards = new ArrayList<>();
    private List<PaperDto> selectedPublications = new ArrayList<>();

    public ProfileDto(Profile profile) {
        this.id = profile.getId();
        this.firstName = profile.getFirstName();
        this.lastName = profile.getLastName();
        this.affiliation = profile.getAffiliation();
        this.email = profile.getEmail();

        if (!CollectionUtils.isEmpty(profile.getEducations())) {
            this.educations = profile.getEducations().stream()
                    .map(ProfileEducationDto::new)
                    .sorted(Comparator.comparing(
                            ProfileEducationDto::isCurrent).reversed()
                            .thenComparing(Comparator.comparing(
                                    ProfileEducationDto::getStartDate).reversed()))
                    .collect(Collectors.toList());
        }

        if (!CollectionUtils.isEmpty(profile.getExperiences())) {
            this.experiences = profile.getExperiences().stream()
                    .map(ProfileExperienceDto::new)
                    .sorted(Comparator.comparing(
                            ProfileExperienceDto::isCurrent).reversed()
                            .thenComparing(Comparator.comparing(
                                    ProfileExperienceDto::getStartDate).reversed()))
                    .collect(Collectors.toList());
        }

        if (!CollectionUtils.isEmpty(profile.getAwards())) {
            this.awards = profile.getAwards().stream()
                    .map(ProfileAwardDto::new)
                    .sorted(Comparator.comparing(
                            ProfileAwardDto::getReceivedDate).reversed())
                    .collect(Collectors.toList());
        }

        if (profile.getMember() != null) {
            this.member = new MemberDto(profile.getMember());
        }
    }

    public Profile toEntity() {
        Profile profile = new Profile();
        profile.setFirstName(this.firstName);
        profile.setLastName(this.lastName);
        profile.setAffiliation(this.affiliation);
        profile.setEmail(this.email);
        return profile;
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

    public void setMine(Long memberId) {
        this.mine = memberId != null && this.member != null && memberId.equals(this.member.getId());
    }

}
