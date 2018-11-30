package io.scinapse.api.data.scinapse.model.oauth;


import io.scinapse.api.data.scinapse.model.BaseEntity;
import io.scinapse.api.data.scinapse.model.Member;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
public class OauthOrcid extends BaseEntity {

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oauthSequence")
    @SequenceGenerator(name = "oauthSequence", sequenceName = "oauth_sequence", allocationSize = 1)
    @Id
    private long id;

    @Column(nullable = false)
    private String uuid = UUID.randomUUID().toString();

    @Column(nullable = false, unique = true)
    private String orcid;

    @Column(nullable = false)
    private String accessToken;

    @Column(nullable = false)
    private boolean connected = false;

    @Transient
    private Map<String, Object> userData;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", unique = true)
    private Member member;

}