package io.scinapse.domain.data.scinapse.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.scinapse.domain.enums.AuthorityName;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Table(schema = "scinapse")
@Entity
public class Authority extends BaseEntity {

    @Id
    private long id;

    @JsonIgnore
    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private AuthorityName name;

}
