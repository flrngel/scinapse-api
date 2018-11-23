package io.scinapse.api.repository.author;

import io.scinapse.api.model.author.AuthorLayerPaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface AuthorLayerPaperRepository extends JpaRepository<AuthorLayerPaper, AuthorLayerPaper.AuthorLayerPaperId>, AuthorLayerPaperRepositoryCustom {

    List<AuthorLayerPaper> findByIdAuthorIdAndIdPaperIdIn(long authorId, Set<Long> paperIds);

    @Query("select lp from AuthorLayerPaper lp where lp.id.authorId = :authorId and lp.selected = true")
    List<AuthorLayerPaper> findSelectedPapers(@Param("authorId") long authorId);

    @Query("select count(lp) from AuthorLayerPaper lp where lp.id.authorId = :authorId and lp.status <> 'PENDING_REMOVE'")
    long getPaperCount(@Param("authorId") long authorId);

    @Query("select lp.id.paperId, lp.paper.title, lp.selected from AuthorLayerPaper lp join lp.paper where lp.id.authorId = :authorId  and lp.status <> 'PENDING_REMOVE' order by lp.paper.citationCount desc")
    List<Object[]> getAllTitles(@Param("authorId") long authorId);

    void deleteByIdAuthorId(long authorId);

}
