package io.scinapse.domain.data.academic;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;

@BatchSize(size = 10)
@Entity
public class PaperAbstract {

    @Id
    private long paperId;

    @Nationalized
    @Column(name = "abstract")
    private String paperAbstract;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId
    private Paper paper;

    public String getAbstract() {
        return this.paperAbstract;
    }

}
