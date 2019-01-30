package io.scinapse.api.data.academic;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Nationalized;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(of = "id")
@BatchSize(size = 50)
@Getter
@Entity
public class Author {

    @Id
    private long id;

    @Nationalized
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
        if (CollectionUtils.isEmpty(authorHIndexHolder)) {
            return null;
        }
        return authorHIndexHolder.get(0);
    }

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "author")
    private List<AuthorTopFos> fosList = new ArrayList<>();

    public long getPaperCount() {
        return Optional.ofNullable(this.paperCount).orElse(0L);
    }

    public long getCitationCount() {
        return Optional.ofNullable(this.citationCount).orElse(0L);
    }

}
