package io.scinapse.api.dto.author;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.data.scinapse.model.author.AuthorLayer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class AuthorInfoDto {
    private long authorId;
    private List<AuthorEducationDto> educations = new ArrayList<>();
    private List<AuthorExperienceDto> experiences = new ArrayList<>();
    private List<AuthorAwardDto> awards = new ArrayList<>();

    public AuthorInfoDto(AuthorLayer layer) {
        List<AuthorEducationDto> educations = layer.getEducations().stream().map(AuthorEducationDto::new).collect(Collectors.toList());
        List<AuthorExperienceDto> experiences = layer.getExperiences().stream().map(AuthorExperienceDto::new).collect(Collectors.toList());
        List<AuthorAwardDto> awards = layer.getAwards().stream().map(AuthorAwardDto::new).collect(Collectors.toList());

        this.authorId = layer.getAuthorId();
        this.educations = educations;
        this.experiences = experiences;
        this.awards = awards;
    }
}
