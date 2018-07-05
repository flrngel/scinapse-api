package network.pluto.absolute.repository.mag;

import network.pluto.absolute.model.mag.Author;
import network.pluto.absolute.model.mag.PaperAuthorAffiliation;

import java.util.List;

public interface AuthorRepositoryCustom {
    List<PaperAuthorAffiliation> getAuthorsByPaperIdIn(List<Long> paperIds);
    List<Author> findCoAuthors(long authorId);
}
