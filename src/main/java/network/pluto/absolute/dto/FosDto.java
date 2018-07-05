package network.pluto.absolute.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.absolute.models.mag.PaperFieldsOfStudy;

@NoArgsConstructor
@Getter
@Setter
public class FosDto {

    private long paperId;
    private long id;
    private String fos;
    private String name;

    public FosDto(PaperFieldsOfStudy fos) {
        this.paperId = fos.getPaper().getId();
        this.id = fos.getFieldsOfStudy().getId();
        this.fos = fos.getFieldsOfStudy().getDisplayName();
        this.name = fos.getFieldsOfStudy().getDisplayName();
    }

}
