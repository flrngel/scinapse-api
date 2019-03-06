package io.scinapse.domain.data.scinapse.model.author;

import io.scinapse.domain.data.scinapse.model.BaseEntity;
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
    private String lastKnownAffiliationName;

    @Column
    private String email;

    @Column(nullable = false)
    private boolean emailHidden = false;

    @Column
    private String bio;

    @Column
    private String webPage;

    @Column
    private String profileImage;

    @Column(nullable = false)
    private long paperCount;

    @Column(nullable = false)
    private long citationCount;

    @Column(nullable = false)
    private int hindex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LayerStatus status = LayerStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_paper", nullable = false)
    private LayerStatus paperStatus = LayerStatus.SYNCED;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<AuthorLayerFos> fosList = new ArrayList<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<AuthorLayerCoauthor> coauthors = new ArrayList<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<AuthorEducation> educations = new ArrayList<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<AuthorExperience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<AuthorAward> awards = new ArrayList<>();

    public enum LayerStatus {
        SYNCED,
        PENDING
    }

}
