package io.scinapse.api.data.academic;

import lombok.Getter;
import org.hibernate.annotations.Nationalized;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

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

}
