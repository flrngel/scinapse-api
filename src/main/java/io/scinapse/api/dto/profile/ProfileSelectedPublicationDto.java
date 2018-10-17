package io.scinapse.api.dto.profile;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.model.profile.ProfileSelectedPublication;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class ProfileSelectedPublicationDto {

    private String profileId;
    private List<PaperDto> papers;

    public ProfileSelectedPublicationDto(ProfileSelectedPublication publication) {
        this.profileId = publication.getId().getProfileId();
    }

}
