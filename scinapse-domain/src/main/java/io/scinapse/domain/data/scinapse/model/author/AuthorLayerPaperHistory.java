package io.scinapse.domain.data.scinapse.model.author;

import io.scinapse.domain.data.scinapse.model.BaseEntity;
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

    @Column
    private long authorId;

    @Column
    private long paperId;

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
