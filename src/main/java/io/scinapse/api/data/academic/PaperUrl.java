package io.scinapse.api.data.academic;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Table(schema = "scinapse", name = "paper_url")
@Entity
public class PaperUrl {

    @Id
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id")
    private Paper paper;

    @Column
    private Integer sourceType;

    @Column
    private String sourceUrl;

}
