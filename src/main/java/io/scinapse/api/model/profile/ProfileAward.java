package io.scinapse.api.model.profile;

import io.scinapse.api.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.Date;

@BatchSize(size = 50)
@Getter
@Setter
@Entity
public class ProfileAward extends BaseEntity {

    @Id
    private String id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @Temporal(TemporalType.DATE)
    @Column
    private Date receivedDate;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

}
