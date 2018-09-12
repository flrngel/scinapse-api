package io.scinapse.api.model.mag;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Table(schema = "scinapse", name = "paper_language")
@Entity
public class PaperLanguage {

    @Id
    private long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id")
    private Paper paper;

    @Column
    private String languageCode;

}
