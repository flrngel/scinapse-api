package io.scinapse.domain.data.scinapse.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@SqlResultSetMapping(name = Comment.WITH_TOTAL_COUNT,
        entities = { @EntityResult(entityClass = Comment.class) },
        columns = { @ColumnResult(name = "total_count") })
@Getter
@Setter
@Table(schema = "scinapse")
@Entity
public class Comment extends BaseEntity {

    public static final String WITH_TOTAL_COUNT = "Comment.withTotalCount";

    @SequenceGenerator(name = "commentSequence", sequenceName = "scinapse.comment_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "commentSequence")
    @Id
    private long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member createdBy;

    @Column
    private Long paperId;

    @Nationalized
    @Type(type = "text")
    @Lob
    @Column(nullable = false)
    private String comment;

}
