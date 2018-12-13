package io.scinapse.api.data.academic;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Getter
@IdClass(PaperReference.PaperReferenceId.class)
@Entity
public class PaperReference {

    @Id
    private long paperId;

    @Id
    private long paperReferenceId;

    @EqualsAndHashCode
    @Getter
    @Setter
    public static class PaperReferenceId implements Serializable {
        private long paperId;
        private long paperReferenceId;
    }

}
