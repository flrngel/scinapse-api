package io.scinapse.api.model.mag;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Table(schema = "scinapse", name = "journal_fos")
@Entity
public class JournalFos {

    @EmbeddedId
    private JournalFosId id;

    @MapsId("journalId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "journalId")
    private Journal journal;

    @MapsId("fosId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "fos_id")
    private FieldsOfStudy fos;

    @Embeddable
    @Getter
    @Setter
    @EqualsAndHashCode
    public static class JournalFosId implements Serializable {

        @Column
        public long journalId;

        @Column
        public long fosId;

    }

}
