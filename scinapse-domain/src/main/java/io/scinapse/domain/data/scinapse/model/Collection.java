package io.scinapse.domain.data.scinapse.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Getter
@Setter
@Table(schema = "scinapse")
@Entity
public class Collection extends BaseEntity {

    @SequenceGenerator(name = "collectionSequence", sequenceName = "scinapse.collection_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "collectionSequence")
    @Id
    private long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member createdBy;

    @Nationalized
    @Column(nullable = false)
    private String title;

    @Nationalized
    @Type(type = "text")
    @Lob
    @Column(nullable = false)
    private String description;

    @Column
    private int paperCount = 0;

}
