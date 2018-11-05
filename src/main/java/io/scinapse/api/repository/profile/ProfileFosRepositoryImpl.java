package io.scinapse.api.repository.profile;

import io.scinapse.api.model.mag.QAuthorTopPaper;
import io.scinapse.api.model.mag.QPaperFieldsOfStudy;
import io.scinapse.api.model.profile.ProfileFos;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import java.util.List;

public class ProfileFosRepositoryImpl extends QueryDslRepositorySupport implements ProfileFosRepositoryCustom {

    public ProfileFosRepositoryImpl() {
        super(ProfileFos.class);
    }

    @Override
    public List<Long> getRelatedFos(List<Long> authorIds) {
        QAuthorTopPaper authorTopPaper = QAuthorTopPaper.authorTopPaper;
        QPaperFieldsOfStudy paperFos = QPaperFieldsOfStudy.paperFieldsOfStudy;

        return from(authorTopPaper)
                .join(authorTopPaper.paper.paperFosList, paperFos)
                .where(authorTopPaper.id.authorId.in(authorIds))
                .groupBy(paperFos.id.fosId)
                .orderBy(paperFos.id.fosId.count().desc())
                .limit(5)
                .select(paperFos.id.fosId)
                .fetch();
    }

}
