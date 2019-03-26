package io.scinapse.api.academic.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.domain.data.academic.model.AuthorTopFos;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class AcAuthorFosDto {

    private long id;
    private String name;
    private Integer rank;

    public AcAuthorFosDto(AuthorTopFos fos) {
        this.id = fos.getFos().getId();
        this.name = fos.getFos().getName();
        this.rank = fos.getRank();
    }

}
