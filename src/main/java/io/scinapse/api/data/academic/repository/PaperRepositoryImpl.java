package io.scinapse.api.data.academic.repository;

import io.scinapse.api.data.academic.Paper;
import io.scinapse.api.data.academic.QPaperFieldsOfStudy;
import io.scinapse.api.data.academic.QPaperTopAuthor;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;

public class PaperRepositoryImpl extends QueryDslRepositorySupport implements PaperRepositoryCustom {

    public PaperRepositoryImpl() {
        super(Paper.class);
    }

    @PersistenceContext(unitName = "academic")
    @Override
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }

    @Override
    public List<Long> calculateFos(Set<Long> paperIds) {
        QPaperFieldsOfStudy paperFos = QPaperFieldsOfStudy.paperFieldsOfStudy;

        return from(paperFos)
                .where(paperFos.id.paperId.in(paperIds))
                .groupBy(paperFos.id.fosId)
                .orderBy(paperFos.id.fosId.count().desc())
                .limit(5)
                .select(paperFos.id.fosId)
                .fetch();
    }

    @Override
    public List<Long> calculateCoauthor(long authorId, Set<Long> paperIds) {
        QPaperTopAuthor topAuthor = QPaperTopAuthor.paperTopAuthor;

        return from(topAuthor)
                .where(topAuthor.id.paperId.in(paperIds)
                        .and(topAuthor.id.authorId.ne(authorId)))
                .groupBy(topAuthor.id.authorId)
                .orderBy(topAuthor.id.authorId.count().desc())
                .limit(5)
                .select(topAuthor.id.authorId)
                .fetch();
    }

}
