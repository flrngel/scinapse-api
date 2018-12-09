package io.scinapse.api.data.scinapse.repository.author;

import io.scinapse.api.data.scinapse.model.author.AuthorLayerPaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface AuthorLayerPaperRepository extends JpaRepository<AuthorLayerPaper, AuthorLayerPaper.AuthorLayerPaperId>, AuthorLayerPaperRepositoryCustom {

    List<AuthorLayerPaper> findByIdAuthorIdAndIdPaperIdIn(long authorId, Set<Long> paperIds);

    @Query("select lp from AuthorLayerPaper lp where lp.id.authorId = :authorId and lp.representative = true")
    List<AuthorLayerPaper> findRepresentativePapers(@Param("authorId") long authorId);

    @Query("select count(lp) from AuthorLayerPaper lp where lp.id.authorId = :authorId and lp.status <> 'PENDING_REMOVE'")
    long getPaperCount(@Param("authorId") long authorId);

    @Query("select lp from AuthorLayerPaper lp where lp.id.authorId = :authorId and lp.status <> 'PENDING_REMOVE'")
    List<AuthorLayerPaper> findAllLayerPapers(@Param("authorId") long authorId);

    void deleteByIdAuthorId(long authorId);

}
