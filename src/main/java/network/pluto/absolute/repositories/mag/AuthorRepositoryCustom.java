package network.pluto.absolute.repositories.mag;

import network.pluto.absolute.models.mag.Author;
import network.pluto.absolute.models.mag.PaperAuthorAffiliation;

import java.util.List;

public interface AuthorRepositoryCustom {
    List<PaperAuthorAffiliation> getAuthorsByPaperIdIn(List<Long> paperIds);
    List<Author> findCoAuthors(long authorId);
}
