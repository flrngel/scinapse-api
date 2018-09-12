package io.scinapse.api.dto.mag;

import io.scinapse.api.model.mag.JournalFos;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class JournalFosDto {

    private long id;
    private String name;

    public JournalFosDto(JournalFos fos) {
        this.id = fos.getFos().getId();
        this.name = fos.getFos().getName();
    }

}
