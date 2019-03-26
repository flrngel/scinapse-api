package io.scinapse.domain.data.scinapse.model.author;

import io.scinapse.domain.data.scinapse.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Table(schema = "scinapse")
@Entity
public class AuthorEducation extends BaseEntity {

    @Id
    private String id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private AuthorLayer author;

    @Temporal(TemporalType.DATE)
    @Column
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Column
    private Date endDate;

    @Column(nullable = false)
    private boolean current;

    @Column
    private Long affiliationId;

    @Nationalized
    @Column(nullable = false)
    private String affiliationName;

    @Nationalized
    @Column
    private String department;

    @Nationalized
    @Column
    private String degree;

}
