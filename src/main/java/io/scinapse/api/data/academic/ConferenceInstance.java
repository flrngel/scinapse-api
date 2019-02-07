package io.scinapse.api.data.academic;

import lombok.Getter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Optional;

@BatchSize(size = 50)
@Getter
@Entity
public class ConferenceInstance {

    @Id
    private long id;

    @Nationalized
    @Column
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private ConferenceSeries conferenceSeries;

    @Nationalized
    @Column
    private String location;

    @Nationalized
    @Column
    private String officialUrl;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column
    private LocalDate abstractRegistrationDate;

    @Column
    private LocalDate submissionDeadlineDate;

    @Column
    private LocalDate notificationDueDate;

    @Column
    private LocalDate finalVersionDueDate;

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
