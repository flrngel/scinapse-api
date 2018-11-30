package io.scinapse.api.data.academic;

import org.hibernate.annotations.BatchSize;

import javax.persistence.*;

@BatchSize(size = 10)
@Table(schema = "scinapse", name = "paper_abstract_new")
@Entity
public class PaperAbstract {

    @Id
    private long paperId;

    @Column(name = "abstract")
    private String paperAbstract;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId
    private Paper paper;

    public String getAbstract() {
        return this.paperAbstract;
    }

}
