package io.scinapse.api.data.scinapse.model.author;

import io.scinapse.api.data.scinapse.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
public class AuthorAward extends BaseEntity {

    @Id
    private String id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private AuthorLayer author;

    @Temporal(TemporalType.DATE)
    @Column
    private Date receivedDate;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

}
