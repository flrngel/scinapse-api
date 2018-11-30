package io.scinapse.api.data.academic;

import lombok.Getter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@BatchSize(size = 10)
@Table(schema = "scinapse", name = "journal")
@Entity
public class Journal {

    @Id
    private long id;

    @Column
    private String title;

    @Column
    private String issn;

    @Column
    private String webPage;

    @Column
    private Long paperCount;

    @Column
    private Long citationCount;

    @Column
    private Double impactFactor;

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "journal")
    private List<JournalFos> fosList = new ArrayList<>();

}
