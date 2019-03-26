package io.scinapse.domain.data.scinapse.model;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Table(schema = "scinapse")
@Entity
public class EmailVerification extends BaseEntity {

    @SequenceGenerator(name = "verificationSequence", sequenceName = "scinapse.verification_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "verificationSequence")
    @Id
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, unique = true)
    private String token;

}
