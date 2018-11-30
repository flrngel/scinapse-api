package io.scinapse.api.data.scinapse.model.author;

import io.scinapse.api.data.academic.FieldsOfStudy;
import io.scinapse.api.data.scinapse.model.BaseEntity;
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

    public AuthorLayerFos(AuthorLayer author, FieldsOfStudy fos) {
        this.author = author;
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
