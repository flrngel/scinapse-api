package network.pluto.absolute.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import network.pluto.absolute.enums.AuthorityName;

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
