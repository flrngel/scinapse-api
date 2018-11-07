package io.scinapse.api.model.mag;

import io.scinapse.api.model.author.AuthorLayer;
import io.scinapse.api.model.profile.ProfileAuthor;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.annotations.BatchSize;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.util.List;

@EqualsAndHashCode(of = "id")
@BatchSize(size = 50)
@Getter
@Table(schema = "scinapse", name = "author")
@Entity
public class Author {

    @Id
    private long id;

    @Column
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private Affiliation lastKnownAffiliation;

    @Column
    private Long paperCount;

    @Column
    private Long citationCount;

    // for lazy loading of one-to-one relation
    @Getter(AccessLevel.PRIVATE)
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "author")
    private List<AuthorLayer> layerHolder;

    public AuthorLayer getLayer() {
        if (CollectionUtils.isEmpty(layerHolder)) {
            return null;
        }
        return layerHolder.get(0);
    }

    // for lazy loading of one-to-one relation
    @Getter(AccessLevel.PRIVATE)
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "author")
    private List<AuthorHIndex> authorHIndexHolder;

    public AuthorHIndex getAuthorHIndex() {
        if (CollectionUtils.isEmpty(authorHIndexHolder)) {
            return null;
        }
        return authorHIndexHolder.get(0);
    }

    @Getter(AccessLevel.PRIVATE)
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "author")
    private List<ProfileAuthor> profileAuthorHolder;

    public ProfileAuthor getProfileAuthor() {
        if (CollectionUtils.isEmpty(profileAuthorHolder)) {
            return null;
        }
        return profileAuthorHolder.get(0);
    }

}
