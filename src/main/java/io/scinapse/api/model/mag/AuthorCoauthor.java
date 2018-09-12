package io.scinapse.api.model.mag;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Table(schema = "scinapse", name = "author_coauthor")
@Entity
public class AuthorCoauthor {

    @EmbeddedId
    private CoauthorId id;

    @MapsId("authorId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    @MapsId("coauthorId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "coauthor_id")
    private Author coauthor;

    @Column
    private Integer rank;

    @Embeddable
    @Getter
    @Setter
    @EqualsAndHashCode
    public static class CoauthorId implements Serializable {

        @Column
        public long authorId;

        @Column
        public long coauthorId;

    }

}
