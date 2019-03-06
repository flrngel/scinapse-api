package io.scinapse.domain.data.scinapse.model;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity(name = "rel_collection_paper")
public class CollectionPaper extends BaseEntity {

    @EmbeddedId
    private CollectionPaperId id;

    @Type(type = "text")
    @Lob
    @Column
    private String note;

    @Column
    private String title;

    @Column
    private Integer year;

    @Column
    private Long citationCount;

    @Embeddable
    @EqualsAndHashCode
    @Getter
    @Setter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(staticName = "of")
    public static class CollectionPaperId implements Serializable {
        @Column
        private long collectionId;
        @Column
        private long paperId;
    }

}
