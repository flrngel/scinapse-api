package io.scinapse.domain.data.academic;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Entity
public class AuthorTopFos {

    @EmbeddedId
    private AuthorTopFosId id;

    @MapsId("authorId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    @MapsId("fosId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fos_id")
    private FieldsOfStudy fos;

    @Column
    private Integer rank;

    @Embeddable
    @Getter
    @Setter
    @EqualsAndHashCode
    public static class AuthorTopFosId implements Serializable {
        @Column
        public long authorId;
        @Column
        public long fosId;
    }

}
