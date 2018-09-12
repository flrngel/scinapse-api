package io.scinapse.api.model.mag;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Table(schema = "scinapse", name = "paper_fos")
@Entity
public class PaperFieldsOfStudy {

    @EmbeddedId
    private PaperFieldsOfStudyId id;

    @MapsId("paperId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "paper_id")
    private Paper paper;

    @MapsId("fosId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "fos_id")
    private FieldsOfStudy fieldsOfStudy;

    @Column
    private Double similarity;

    @EqualsAndHashCode
    @Getter
    @Setter
    @Embeddable
    public static class PaperFieldsOfStudyId implements Serializable {

        @Column
        private long paperId;

        @Column
        private long fosId;

    }

}
