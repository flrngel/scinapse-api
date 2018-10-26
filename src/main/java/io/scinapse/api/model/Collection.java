package io.scinapse.api.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class Collection extends BaseEntity {

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "collectionSequence")
    @SequenceGenerator(name = "collectionSequence", sequenceName = "collection_sequence", allocationSize = 1)
    @Id
    private long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member createdBy;

    @Column(nullable = false)
    private String title;

    @Type(type = "text")
    @Lob
    @Column(nullable = false)
    private String description;

    @Column
    private int paperCount = 0;

    @Column
    private boolean isDefault = false;

}
