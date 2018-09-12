package io.scinapse.api.model.mag;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Table(schema = "scinapse", name = "fields_of_study")
@Entity
public class FieldsOfStudy {

    @Id
    private long id;

    @Column
    private String name;

    @Column
    private Integer level;

    @Column
    private Long paperCount;

    @Column
    private Long citationCount;

}
