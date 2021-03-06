package io.scinapse.domain.data.academic.model;

import lombok.Getter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Nationalized;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Optional;

@Getter
@BatchSize(size = 50)
@Entity
public class Affiliation {

    @Id
    private long id;

    @Nationalized
    @Column
    private String name;

    @Column
    private String officialPage;

    @Column
    private String wikiPage;

    @Column
    private Long paperCount;

    @Column
    private Long citationCount;

    public long getPaperCount() {
        return Optional.ofNullable(this.paperCount).orElse(0L);
    }

    public long getCitationCount() {
        return Optional.ofNullable(this.citationCount).orElse(0L);
    }

}
