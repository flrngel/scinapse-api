package io.scinapse.api.data.scinapse.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.scinapse.api.enums.AuthorityName;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class Authority extends BaseEntity {

    @Id
    private long id;

    @JsonIgnore
    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private AuthorityName name;

}
