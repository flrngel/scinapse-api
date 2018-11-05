package io.scinapse.api.model.profile;

import io.scinapse.api.model.mag.FieldsOfStudy;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class ProfileFos {

    @EmbeddedId
    private ProfileFosId id;

    @MapsId("profileId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @MapsId("fosId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fos_id")
    private FieldsOfStudy fos;

    public ProfileFos(Profile profile, FieldsOfStudy fos) {
        this.profile = profile;
        this.fos = fos;
        this.id = ProfileFosId.of(profile.getId(), fos.getId());
    }

    @Embeddable
    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(staticName = "of")
    public static class ProfileFosId implements Serializable {
        @Column
        private String profileId;
        @Column
        private long fosId;
    }

}
