package io.scinapse.api.dto.author;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.data.scinapse.model.author.AuthorLayer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
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
        this.authorId = layer.getAuthorId();

        this.educations = layer.getEducations()
                .stream()
                .map(AuthorEducationDto::new)
                .sorted(Comparator.comparing(AuthorEducationDto::isCurrent)
                        .thenComparing(AuthorEducationDto::getStartDate).reversed())
                .collect(Collectors.toList());

        this.experiences = layer.getExperiences()
                .stream()
                .map(AuthorExperienceDto::new)
                .sorted(Comparator.comparing(AuthorExperienceDto::isCurrent)
                        .thenComparing(AuthorExperienceDto::getStartDate).reversed())
                .collect(Collectors.toList());

        this.awards = layer.getAwards()
                .stream()
                .map(AuthorAwardDto::new)
                .sorted(Comparator.comparing(AuthorAwardDto::getReceivedDate).reversed())
                .collect(Collectors.toList());
    }
}
