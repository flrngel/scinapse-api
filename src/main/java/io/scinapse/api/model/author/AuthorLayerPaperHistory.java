package io.scinapse.api.model.author;

import io.scinapse.api.model.BaseEntity;
import io.scinapse.api.model.mag.Paper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class AuthorLayerPaperHistory extends BaseEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private long authorId;

    @Column(name = "paper_id", nullable = false, insertable = false, updatable = false)
    private long paperId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id")
    private Paper paper;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaperAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionStatus status = ActionStatus.PENDING;

    public enum PaperAction {
        REMOVE,
        ADD
    }

    public enum ActionStatus {
        ACCEPTED,
        PENDING
    }

}
