package io.scinapse.api.model.author;

import io.scinapse.api.model.BaseEntity;
import io.scinapse.api.model.mag.Affiliation;
import io.scinapse.api.model.mag.Paper;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class AuthorLayerPaper extends BaseEntity {

    @EmbeddedId
    private AuthorLayerPaperId id;

    @MapsId("authorId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private AuthorLayer authorLayer;

    @MapsId("paperId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id")
    private Paper paper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliation_id")
    private Affiliation affiliation;

    @Column
    private Integer authorSequenceNumber;

    @Column
    private boolean selected = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaperStatus status = PaperStatus.SYNCED;

    public AuthorLayerPaper(AuthorLayer authorLayer, Paper paper) {
        this.authorLayer = authorLayer;
        this.paper = paper;
        this.id = AuthorLayerPaperId.of(authorLayer.getAuthorId(), paper.getId());
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
