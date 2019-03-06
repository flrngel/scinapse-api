package io.scinapse.domain.data.academic;

import lombok.Getter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Nationalized;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@BatchSize(size = 50)
@Getter
@Entity
public class ConferenceSeries {

    @Id
    private long id;

    @Nationalized
    @Column
    private String name;

    @Column
    private Long paperCount;

    @Column
    private Long citationCount;

    @BatchSize(size = 10)
    @OneToMany(mappedBy = "conferenceSeries")
    private List<ConferenceInstance> conferenceInstanceList = new ArrayList<>();

    public long getPaperCount() {
        return Optional.ofNullable(this.paperCount).orElse(0L);
    }

    public long getCitationCount() {
        return Optional.ofNullable(this.citationCount).orElse(0L);
    }

}
