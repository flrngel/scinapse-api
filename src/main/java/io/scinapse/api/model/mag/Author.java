package io.scinapse.api.model.mag;

import io.scinapse.api.model.profile.ProfileAuthor;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.annotations.BatchSize;

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
    private List<AuthorHIndex> authorHIndexHolder;

    public AuthorHIndex getAuthorHIndex() {
        if (authorHIndexHolder == null || authorHIndexHolder.size() == 0) {
            return null;
        }
        return authorHIndexHolder.get(0);
    }

    @Getter(AccessLevel.PRIVATE)
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "author")
    private List<ProfileAuthor> profileAuthorHolder;

    public ProfileAuthor getProfileAuthor() {
        if (profileAuthorHolder == null || profileAuthorHolder.size() == 0) {
            return null;
        }
        return profileAuthorHolder.get(0);
    }

}
