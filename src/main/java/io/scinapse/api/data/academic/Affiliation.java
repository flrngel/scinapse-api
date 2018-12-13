package io.scinapse.api.data.academic;

import lombok.Getter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Nationalized;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@BatchSize(size = 50)
@Entity
public class Affiliation {

    @Id
    private long id;

    @Nationalized
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
