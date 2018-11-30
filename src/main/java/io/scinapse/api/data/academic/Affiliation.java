package io.scinapse.api.data.academic;

import lombok.Getter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@BatchSize(size = 50)
@Table(schema = "scinapse", name = "affiliation")
@Entity
public class Affiliation {

    @Id
    private long id;

    @Column
    private String name;

    @Column
    private String officialPage;

    @Column
    private String wikiPage;

    @Column
    private Long paperCount;

    @Column
    private Long citationCount;

}
