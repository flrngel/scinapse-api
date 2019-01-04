package io.scinapse.api.data.academic;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Nationalized;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@BatchSize(size = 10)
@Getter
@Setter
@Entity
public class Paper {

    @Id
    private long id;

    @Column
    private String doi;

    @Nationalized
    @Column
    private String title;

    @Nationalized
    @Column
    private String bookTitle;

    @Column
    private Integer year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_id")
    private Journal journal;

    @Column
    private String volume;

    @Column
    private String issue;

    @Column
    private String firstPage;

    @Column
    private String lastPage;

    @Column
    private Long referenceCount;

    @Column
    private Long citationCount;

    @BatchSize(size = 10)
    @OneToMany(mappedBy = "paper")
    private List<PaperTopAuthor> authors = new ArrayList<>();

    @BatchSize(size = 10)
    @OneToMany(mappedBy = "paper")
    private List<PaperFieldsOfStudy> paperFosList = new ArrayList<>();

    @BatchSize(size = 10)
    @OneToMany(mappedBy = "paper")
    private List<PaperUrl> paperUrls = new ArrayList<>();

    @Getter(AccessLevel.PRIVATE)
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "paper")
    private List<PaperAbstract> paperAbstractHolder;

    public PaperAbstract getPaperAbstract() {
        if (CollectionUtils.isEmpty(paperAbstractHolder)) {
            return null;
        }
        return paperAbstractHolder.get(0);
    }

    public long getReferenceCount() {
        return Optional.ofNullable(this.referenceCount).orElse(0L);
    }

    public long getCitationCount() {
        return Optional.ofNullable(this.citationCount).orElse(0L);
    }

}
