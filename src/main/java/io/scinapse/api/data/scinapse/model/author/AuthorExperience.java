package io.scinapse.api.data.scinapse.model.author;

import io.scinapse.api.data.scinapse.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
public class AuthorExperience extends BaseEntity {

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

    @Column(nullable = false)
    private String affiliationName;

    @Column
    private String department;

    @Column
    private String position;

    @Column
    private String description;

}
