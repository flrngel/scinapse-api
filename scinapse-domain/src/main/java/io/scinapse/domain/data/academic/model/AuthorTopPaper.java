package io.scinapse.domain.data.academic.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Entity
public class AuthorTopPaper {

    @EmbeddedId
    private AuthorTopPaperId id;

    @MapsId("authorId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    @MapsId("paperId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id")
    private Paper paper;

    @EqualsAndHashCode
    @Getter
    @Setter
    @Embeddable
    public static class AuthorTopPaperId implements Serializable {
        @Column
        private long authorId;
        @Column
        private long paperId;
    }

}
