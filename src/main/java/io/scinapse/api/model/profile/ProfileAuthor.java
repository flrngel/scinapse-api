package io.scinapse.api.model.profile;

import io.scinapse.api.model.mag.Author;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@ToString(of = "id")
@Getter
@Setter
@Entity
@NoArgsConstructor
public class ProfileAuthor {

    @EmbeddedId
    private ProfileAuthorId id;

    @MapsId("profileId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", insertable = false, updatable = false)
    private Profile profile;

    @MapsId("authorId")
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private Author author;

    public ProfileAuthor(Profile profile, Author author) {
        this.profile = profile;
        this.author = author;
        this.id = ProfileAuthorId.of(profile.getId(), author.getId());
    }

    @ToString
    @Embeddable
    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(staticName = "of")
    public static class ProfileAuthorId implements Serializable {
        @Column
        private String profileId;
        @Column
        private long authorId;
    }

}
