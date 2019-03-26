package io.scinapse.domain.data.scinapse.model.oauth;

import io.scinapse.domain.data.scinapse.model.BaseEntity;
import io.scinapse.domain.data.scinapse.model.Member;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Table(schema = "scinapse")
@Entity
public class OauthGoogle extends BaseEntity {

    @SequenceGenerator(name = "oauthSequence", sequenceName = "scinapse.oauth_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oauthSequence")
    @Id
    private long id;

    @Column(nullable = false)
    private String uuid = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String googleId;

    @Column(nullable = false)
    private String accessToken;

    @Column(nullable = false)
    private boolean connected = false;

    @Transient
    private Map<String, Object> userData;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", unique = true)
    private Member member;

}
