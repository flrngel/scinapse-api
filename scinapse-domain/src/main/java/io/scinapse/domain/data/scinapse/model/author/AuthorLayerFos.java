package io.scinapse.domain.data.scinapse.model.author;

import io.scinapse.domain.data.scinapse.model.BaseEntity;
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

    @Column
    private int rank;

    public AuthorLayerFos(AuthorLayer author, long fosId, int rank) {
        this.author = author;
        this.rank = rank;
        this.id = AuthorLayerFosId.of(author.getAuthorId(), fosId);
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
