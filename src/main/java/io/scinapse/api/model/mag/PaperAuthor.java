package io.scinapse.api.model.mag;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Table(schema = "scinapse", name = "paper_author")
@Entity
public class PaperAuthor {

    @EmbeddedId
    private PaperAuthorId id;

    @Column(name = "paper_id", insertable = false, updatable = false)
    private long paperId;

    @MapsId("paperId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id")
    private Paper paper;

    @MapsId("authorId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliation_id")
    private Affiliation affiliation;

    @Column
    private Integer authorSequenceNumber;

    @EqualsAndHashCode
    @Getter
    @Setter
    @Embeddable
    public static class PaperAuthorId implements Serializable {

        @Column
        private long paperId;

        @Column
        private long authorId;

    }

}
