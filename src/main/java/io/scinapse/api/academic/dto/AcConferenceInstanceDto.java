package io.scinapse.api.academic.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.data.academic.ConferenceInstance;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class AcConferenceInstanceDto {
    private long id;
    private AcConferenceSeriesDto conferenceSeries;
    private String name;
    private String location;
    private String officialUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate abstractRegistrationDate;
    private LocalDate submissionDeadlineDate;
    private LocalDate notificationDueDate;
    private LocalDate finalVersionDueDate;
    private long paperCount;
    private long citationCount;

    public AcConferenceInstanceDto(ConferenceInstance conferenceInstance) {
        this.id = conferenceInstance.getId();
        this.conferenceSeries = new AcConferenceSeriesDto(conferenceInstance.getConferenceSeries());
        this.name = conferenceInstance.getName();
        this.location = conferenceInstance.getLocation();
        this.officialUrl = conferenceInstance.getOfficialUrl();
        this.startDate = conferenceInstance.getStartDate();
        this.endDate = conferenceInstance.getEndDate();
        this.abstractRegistrationDate = conferenceInstance.getAbstractRegistrationDate();
        this.submissionDeadlineDate = conferenceInstance.getSubmissionDeadlineDate();
        this.notificationDueDate = conferenceInstance.getNotificationDueDate();
        this.finalVersionDueDate = conferenceInstance.getFinalVersionDueDate();
        this.paperCount = conferenceInstance.getPaperCount();
        this.citationCount = conferenceInstance.getCitationCount();
    }
}
