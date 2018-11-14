package io.scinapse.api.repository.author;

import io.scinapse.api.model.author.AuthorLayerFos;
import io.scinapse.api.model.mag.QAuthorTopPaper;
import io.scinapse.api.model.mag.QPaperFieldsOfStudy;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import java.util.List;

public class AuthorLayerFosRepositoryImpl extends QueryDslRepositorySupport implements AuthorLayerFosRepositoryCustom {

    public AuthorLayerFosRepositoryImpl() {
        super(AuthorLayerFos.class);
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
