package io.scinapse.domain.data.academic;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Entity
public class PaperTopAuthor {

    @EmbeddedId
    private PaperTopAuthorId id;

    @Column(name = "paper_id", insertable = false, updatable = false)
    private long paperId;

    @MapsId("paperId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id")
    private Paper paper;

    @NotFound(action = NotFoundAction.IGNORE)
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
    public static class PaperTopAuthorId implements Serializable {
        @Column
        private long paperId;
        @Column
        private long authorId;
    }

}
