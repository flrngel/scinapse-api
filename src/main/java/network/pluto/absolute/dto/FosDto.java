package network.pluto.absolute.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.bibliotheca.models.Fos;
import network.pluto.bibliotheca.models.mag.PaperFieldsOfStudy;

@NoArgsConstructor
@Getter
@Setter
public class FosDto {

    private long paperId;
    private long id;
    private String fos;
    private String name;

    public FosDto(Fos fos) {
        this.id = fos.getId();
        this.fos = fos.getFos();
    }

    public FosDto(PaperFieldsOfStudy fos) {
        this.paperId = fos.getPaper().getId();
        this.id = fos.getFieldsOfStudy().getId();
        this.fos = fos.getFieldsOfStudy().getDisplayName();
        this.name = fos.getFieldsOfStudy().getDisplayName();
    }

}
