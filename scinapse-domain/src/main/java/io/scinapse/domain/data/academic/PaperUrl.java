package io.scinapse.domain.data.academic;

import lombok.Getter;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Getter
@Entity
public class PaperUrl {

    @Id
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id")
    private Paper paper;

    @Column
    private Integer sourceType;

    @Type(type = "text")
    @Lob
    @Column
    private String sourceUrl;

    @Column
    private boolean isPdf;

}
