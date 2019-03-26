package io.scinapse.domain.data.scinapse.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@BatchSize(size = 50)
@Getter
@Setter
@Table(schema = "scinapse")
@Entity
public class Member extends BaseEntity {

    @SequenceGenerator(name = "memberSequence", sequenceName = "scinapse.member_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "memberSequence")
    @Id
    private long id;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column
    private String password;

    @JsonIgnore
    @BatchSize(size = 10)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(schema = "scinapse",
            name = "rel_member_authority",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id"))
    private List<Authority> authorities = new ArrayList<>();

    @Nationalized
    @Column(nullable = false)
    private String firstName;

    @Nationalized
    @Column(nullable = false)
    private String lastName;

    @Column
    private String profileImage;

    @Column
    private Long affiliationId;

    @Nationalized
    @Column(nullable = false)
    private String affiliationName;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(name = "author_id")
    private Long authorId;

    public String getFullName() {
        // existing user does not have a last name.
        if (StringUtils.isBlank(this.lastName)) {
            return this.firstName;
        }
        return this.firstName + " " + this.lastName;
    }

}
