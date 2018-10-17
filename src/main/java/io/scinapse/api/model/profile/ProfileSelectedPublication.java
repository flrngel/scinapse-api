package io.scinapse.api.model.profile;

import io.scinapse.api.model.BaseEntity;
import io.scinapse.api.model.mag.Paper;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class ProfileSelectedPublication extends BaseEntity {

    @EmbeddedId
    private ProfileSelectedPublicationId id;

    @MapsId("profileId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @MapsId("paperId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id")
    private Paper paper;

    public ProfileSelectedPublication(Profile profile, Paper paper) {
        this.profile = profile;
        this.paper = paper;
        this.id = ProfileSelectedPublicationId.of(profile.getId(), paper.getId());
    }

    @Embeddable
    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(staticName = "of")
    public static class ProfileSelectedPublicationId implements Serializable {
        @Column
        private String profileId;
        @Column
        private long paperId;
    }

}
