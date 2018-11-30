package io.scinapse.api.data.academic.repository;

import io.scinapse.api.data.academic.Author;
import io.scinapse.api.data.academic.QAuthorTopPaper;
import io.scinapse.api.data.academic.QPaperFieldsOfStudy;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class AuthorRepositoryImpl extends QueryDslRepositorySupport implements AuthorRepositoryCustom {

    public AuthorRepositoryImpl() {
        super(Author.class);
    }

    @PersistenceContext(unitName = "academic")
    @Override
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }

    @Override
    public List<Long> getRelatedFos(long authorId) {
        QAuthorTopPaper authorTopPaper = QAuthorTopPaper.authorTopPaper;
        QPaperFieldsOfStudy paperFos = QPaperFieldsOfStudy.paperFieldsOfStudy;

        return from(authorTopPaper)
                .join(authorTopPaper.paper.paperFosList, paperFos)
                .where(authorTopPaper.id.authorId.eq(authorId))
                .groupBy(paperFos.id.fosId)
                .orderBy(paperFos.id.fosId.count().desc())
                .limit(5)
                .select(paperFos.id.fosId)
                .fetch();
    }

}
