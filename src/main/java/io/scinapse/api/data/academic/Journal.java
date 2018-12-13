package io.scinapse.api.data.academic;

import lombok.Getter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Nationalized;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Getter
@BatchSize(size = 10)
@Entity
public class Journal {

    @Id
    private long id;

    @Nationalized
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
