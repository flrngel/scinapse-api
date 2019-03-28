package io.scinapse.domain.data.scinapse.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;

@Getter
@Setter
@Table(schema = "scinapse")
@Entity
public class PaperRequest extends BaseEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;

    @Column(nullable = false)
    private long paperId;

    @Column
    private String email;

    @Nationalized
    @Column
    private String name;

    @Nationalized
    @Column
    private String message;

    @Column
    private Long memberId;

}
