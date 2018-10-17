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
public class ProfileExperience extends BaseEntity {

    @Id
    private String id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @Temporal(TemporalType.DATE)
    @Column
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Column
    private Date endDate;

    @Column(nullable = false)
    private boolean current;

    @Column(nullable = false)
    private String institution;

    @Column
    private String department;

    @Column
    private String position;

}
