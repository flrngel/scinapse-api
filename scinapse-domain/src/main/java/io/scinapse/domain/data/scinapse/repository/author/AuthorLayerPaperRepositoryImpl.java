package io.scinapse.domain.data.scinapse.repository.author;

import io.scinapse.domain.data.scinapse.model.author.AuthorLayerPaper;
import io.scinapse.domain.enums.PaperSort;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import javax.persistence.Query;
import java.math.BigInteger;
import java.util.List;

public class AuthorLayerPaperRepositoryImpl extends QueryDslRepositorySupport implements AuthorLayerPaperRepositoryCustom {

    public AuthorLayerPaperRepositoryImpl() {
        super(AuthorLayerPaper.class);
    }

    @Override
    public Page<AuthorLayerPaper> findPapers(long authorId, boolean showAll, String[] keywords, PaperSort sort, Pageable pageable) {
        String sql = "select lp.*\n" +
                "from scinapse.author_layer_paper lp\n" +
                "where lp.author_id = :authorId";
        String count = "select count(1) from scinapse.author_layer_paper lp where lp.author_id = :authorId";

        if (!showAll) {
            sql += "\nand lp.status <> 'PENDING_REMOVE'";
            count += "\nand lp.status <> 'PENDING_REMOVE'";
        }

        if (keywords != null && keywords.length > 0) {
            sql += "\nand contains(lp.title, :keywords)";
            count += "\nand contains(lp.title, :keywords)";
        }

        if (sort == null) {
            sort = PaperSort.NEWEST_FIRST;
        }
        switch (sort) {
            case RECENTLY_ADDED:
                // desc defaults to nulls last
                sql += "\norder by lp.updated_at desc";
                break;
            case MOST_CITATIONS:
                sql += "\norder by lp.citation_count desc";
                break;
            case OLDEST_FIRST:
                // SQL Server asc nulls last workaround
                sql += "\norder by case when lp.year is null then 2 else 1 end, lp.year asc";
                break;
            default:
                sql += "\norder by lp.year desc";
                break;
        }

        Query countQuery = getEntityManager()
                .createNativeQuery(count)
                .setParameter("authorId", authorId);

        Query query = getEntityManager()
                .createNativeQuery(sql, AuthorLayerPaper.class)
                .setParameter("authorId", authorId);

        if (keywords != null && keywords.length > 0) {
            String keywordStr = StringUtils.join(keywords, " AND ");
            query.setParameter("keywords", keywordStr);
            countQuery.setParameter("keywords", keywordStr);
        }

        int totalCount = (int) countQuery.getSingleResult();

        List results = query
                .setFirstResult(pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(results, pageable, totalCount);
    }

}
