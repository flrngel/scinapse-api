package io.scinapse.api.repository.profile;

import com.querydsl.core.types.Projections;
import io.scinapse.api.controller.ProfileController;
import io.scinapse.api.model.mag.QAuthor;
import io.scinapse.api.model.mag.QAuthorTopPaper;
import io.scinapse.api.model.profile.Profile;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import java.util.List;

public class ProfileRepositoryImpl extends QueryDslRepositorySupport implements ProfileRepositoryCustom {

    public ProfileRepositoryImpl() {
        super(Profile.class);
    }

    @Override
    public List<ProfileController.PaperTitleDto> getAllProfilePapers(String profileId) {
        QAuthor author = QAuthor.author;
        QAuthorTopPaper authorTopPaper = QAuthorTopPaper.authorTopPaper;

        List<Long> authorId = from(author)
                .where(author.profileId.eq(profileId))
                .select(author.id)
                .fetch();

        return from(authorTopPaper)
                .join(authorTopPaper.paper)
                .where(authorTopPaper.author.id.in(authorId))
                .select(Projections.constructor(ProfileController.PaperTitleDto.class, authorTopPaper.paper.id, authorTopPaper.paper.title))
                .fetch();
    }

}
