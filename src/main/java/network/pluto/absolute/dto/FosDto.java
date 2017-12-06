package network.pluto.absolute.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.bibliotheca.models.Fos;

@NoArgsConstructor
@Getter
@Setter
public class FosDto {
    private long id;
    private String fos;

    public FosDto(Fos fos) {
        this.id = fos.getId();
        this.fos = fos.getFos();
    }
}
