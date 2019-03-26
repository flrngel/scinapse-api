package io.scinapse.domain.data.scinapse.model.author;

import io.scinapse.domain.data.scinapse.model.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Table(schema = "scinapse")
@Entity
public class AuthorLayerPaper extends BaseEntity {

    @EmbeddedId
    private AuthorLayerPaperId id;

    @Column
    private Long affiliationId;

    @Column
    private Integer authorSequenceNumber;

    @Column
    private String title;

    @Column
    private Integer year;

    @Column
    private Long citationCount;

    @Column
    private boolean representative = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaperStatus status = PaperStatus.SYNCED;

    public AuthorLayerPaper(long authorId, long paperId) {
        this.id = AuthorLayerPaperId.of(authorId, paperId);
    }

    @Embeddable
    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(staticName = "of")
    public static class AuthorLayerPaperId implements Serializable {
        @Column
        private long authorId;
        @Column
        private long paperId;
    }

    public enum PaperStatus {
        SYNCED,
        PENDING_REMOVE,
        PENDING_ADD
    }

}
