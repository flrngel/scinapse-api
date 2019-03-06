package io.scinapse.domain.data.academic;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Entity
public class PaperRecommendation {

    @EmbeddedId
    private PaperRecommendationId id;

    @MapsId("paperId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="paper_id")
    private Paper paper;

    @MapsId("recommendedPaperId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="recommended_paper_id")
    private Paper recommendedPaper;

    @Column
    private Double score;

    @Embeddable
    @Getter
    @Setter
    @EqualsAndHashCode
    public static class PaperRecommendationId implements Serializable {
        @Column
        public long paperId;

        @Column
        public long recommendedPaperId;
    }

}
