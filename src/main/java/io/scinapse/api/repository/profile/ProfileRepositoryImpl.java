package io.scinapse.api.repository.profile;

import com.querydsl.core.types.Projections;
import io.scinapse.api.controller.ProfileController;
import io.scinapse.api.model.mag.QPaperAuthor;
import io.scinapse.api.model.profile.Profile;
import io.scinapse.api.model.profile.QProfileAuthor;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import java.util.List;

public class ProfileRepositoryImpl extends QueryDslRepositorySupport implements ProfileRepositoryCustom {

    public ProfileRepositoryImpl() {
        super(Profile.class);
    }

    @Override
    public List<ProfileController.PaperTitleDto> getAllProfilePapers(String profileId) {
        QProfileAuthor profileAuthor = QProfileAuthor.profileAuthor;
        QPaperAuthor paperAuthor = QPaperAuthor.paperAuthor;

        List<Long> authorId = from(profileAuthor)
                .where(profileAuthor.id.profileId.eq(profileId))
                .select(profileAuthor.id.authorId)
                .fetch();

        return from(paperAuthor)
                .join(paperAuthor.paper)
                .where(paperAuthor.author.id.in(authorId))
                .select(Projections.constructor(ProfileController.PaperTitleDto.class, paperAuthor.paper.id, paperAuthor.paper.title))
                .fetch();
    }

}
