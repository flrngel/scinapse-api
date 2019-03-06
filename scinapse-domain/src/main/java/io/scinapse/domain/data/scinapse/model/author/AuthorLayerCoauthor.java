package io.scinapse.domain.data.scinapse.model.author;

import io.scinapse.domain.data.scinapse.model.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class AuthorLayerCoauthor extends BaseEntity {

    @EmbeddedId
    private AuthorLayerCoauthorId id;

    @MapsId("authorId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private AuthorLayer author;

    @Column
    private int rank;

    public AuthorLayerCoauthor(AuthorLayer author, long coauthorId, int rank) {
        this.author = author;
        this.rank = rank;
        this.id = AuthorLayerCoauthorId.of(author.getAuthorId(), coauthorId);
    }

    @Embeddable
    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(staticName = "of")
    public static class AuthorLayerCoauthorId implements Serializable {
        @Column
        private long authorId;
        @Column
        private long coauthorId;
    }

}
