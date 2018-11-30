package io.scinapse.api.data.academic;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@IdClass(PaperReference.PaperReferenceId.class)
@Table(schema = "scinapse", name = "paper_reference")
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
