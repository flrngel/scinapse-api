package io.scinapse.api.model.author;

import io.scinapse.api.model.BaseEntity;
import io.scinapse.api.model.mag.FieldsOfStudy;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class AuthorLayerFos extends BaseEntity {

    @EmbeddedId
    private AuthorLayerFosId id;

    @MapsId("authorId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private AuthorLayer author;

    @MapsId("fosId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fos_id")
    private FieldsOfStudy fos;

    public AuthorLayerFos(AuthorLayer author, FieldsOfStudy fos) {
        this.author = author;
        this.fos = fos;
        this.id = AuthorLayerFosId.of(author.getAuthorId(), fos.getId());
    }

    @Embeddable
    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(staticName = "of")
    public static class AuthorLayerFosId implements Serializable {
        @Column
        private long authorId;
        @Column
        private long fosId;
    }

}
