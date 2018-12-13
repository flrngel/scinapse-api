package io.scinapse.api.data.academic;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@IdClass(PaperRecommendation.PaperRecommendationId.class)
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
