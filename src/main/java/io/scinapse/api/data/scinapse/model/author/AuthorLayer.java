package io.scinapse.api.data.scinapse.model.author;

import io.scinapse.api.data.scinapse.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class AuthorLayer extends BaseEntity {

    @Id
    private long authorId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column
    private Long lastKnownAffiliationId;

    @Column
    private String email;

    @Column
    private String bio;

    @Column
    private String webPage;

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

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<AuthorLayerFos> fosList = new ArrayList<>();

    public enum LayerStatus {
        SYNCED,
        PENDING
    }

}