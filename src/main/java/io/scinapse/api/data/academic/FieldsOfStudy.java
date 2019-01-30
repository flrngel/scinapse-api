package io.scinapse.api.data.academic;

import lombok.Getter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Nationalized;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Optional;

@BatchSize(size = 50)
@Getter
@Entity
public class FieldsOfStudy {

    @Id
    private long id;

    @Nationalized
    @Column
    private String name;

    @Column
    private Integer level;

    @Column
    private Long paperCount;

    @Column
    private Long citationCount;

    public long getPaperCount() {
        return Optional.ofNullable(this.paperCount).orElse(0L);
    }

    public long getCitationCount() {
        return Optional.ofNullable(this.citationCount).orElse(0L);
    }

}
