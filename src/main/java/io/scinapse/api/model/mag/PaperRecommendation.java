package io.scinapse.api.model.mag;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@IdClass(PaperRecommendation.PaperRecommendationId.class)
@Table(schema = "scinapse", name = "paper_recommendation")
@Entity
public class PaperRecommendation {

    @Id
    private long paperId;

    @Id
    private long recommendedPaperId;

    @Column
    private Double score;

    @Embeddable
    @EqualsAndHashCode
    public static class PaperRecommendationId implements Serializable {
        public long paperId;
        public long recommendedPaperId;
    }

}
