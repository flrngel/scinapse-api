package io.scinapse.api.model.author;

import io.scinapse.api.model.BaseEntity;
import io.scinapse.api.model.mag.Affiliation;
import io.scinapse.api.model.mag.Author;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class AuthorLayer extends BaseEntity {

    @Id
    private long authorId;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private Affiliation lastKnownAffiliation;

    @Column
    private String bio;

    @Column(nullable = false)
    private long paperCount;

    @Column(nullable = false)
    private long citationCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LayerStatus status = LayerStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_paper", nullable = false)
    private LayerStatus paperStatus = LayerStatus.SYNCED;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId
    private Author author;

    public enum LayerStatus {
        SYNCED,
        PENDING
    }

}
