package io.scinapse.domain.data.scinapse.repository.author;

import io.scinapse.domain.data.scinapse.model.author.AuthorLayerPaper;
import io.scinapse.domain.enums.PaperSort;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.List;

import static io.scinapse.domain.enums.PaperSort.*;

public class AuthorLayerPaperRepositoryImpl extends QueryDslRepositorySupport implements AuthorLayerPaperRepositoryCustom {

    public AuthorLayerPaperRepositoryImpl() {
        super(AuthorLayerPaper.class);
    }

    @PersistenceContext(unitName = "scinapse")
    @Override
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }

    @Override
    public Page<AuthorLayerPaper> findPapers(long authorId, boolean showAll, String[] keywords, PaperSort sort, Pageable pageable) {
        String sql = "select lp.*\n" +
                "from author_layer_paper lp\n" +
                "where lp.author_id = :authorId";
        String count = "select count(1) from author_layer_paper lp where lp.author_id = :authorId";

        if (!showAll) {
            sql += "\nand lp.status <> 'PENDING_REMOVE'";
            count += "\nand lp.status <> 'PENDING_REMOVE'";
        }

        if (keywords != null && keywords.length > 0) {
            sql += "\nand to_tsvector(lp.title) @@ to_tsquery(:keywords)";
            count += "\nand to_tsvector(lp.title) @@ to_tsquery(:keywords)";
        }

        if (sort == null) {
            sort = PaperSort.NEWEST_FIRST;
        }
        switch (sort) {
            case RECENTLY_ADDED:
                sql += "\norder by lp.updated_at desc nulls last";
                break;
            case MOST_CITATIONS:
                sql += "\norder by lp.citation_count desc nulls last";
                break;
            case OLDEST_FIRST:
                sql += "\norder by lp.year asc nulls last";
                break;
            default:
                sql += "\norder by lp.year desc nulls last";
                break;
        }

        Query countQuery = getEntityManager()
                .createNativeQuery(count)
                .setParameter("authorId", authorId);

        Query query = getEntityManager()
                .createNativeQuery(sql, AuthorLayerPaper.class)
                .setParameter("authorId", authorId);

        if (keywords != null && keywords.length > 0) {
            String keywordStr = StringUtils.join(keywords, " & ");
            query.setParameter("keywords", keywordStr);
            countQuery.setParameter("keywords", keywordStr);
        }

        long totalCount = ((BigInteger) countQuery.getSingleResult()).longValue();

        List results = query
                .setFirstResult(pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(results, pageable, totalCount);
    }

}
