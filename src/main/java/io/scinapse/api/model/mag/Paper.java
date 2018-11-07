package io.scinapse.api.model.mag;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@BatchSize(size = 10)
@Getter
@Setter
@Table(schema = "scinapse", name = "paper")
@Entity
public class Paper {

    @Id
    private long id;

    @Column
    private String doi;

    @Column
    private String title;

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
    private Long paperCount;

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
    private List<PaperLanguage> paperLanguages = new ArrayList<>();

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

}
