package io.scinapse.api.data.scinapse.repository.author;

import io.scinapse.api.data.scinapse.model.author.AuthorLayerPaper;
import io.scinapse.api.enums.PaperSort;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

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
    public List<AuthorLayerPaper> findPapers(long authorId, boolean showAll, String[] keywords, PaperSort sort, Pageable pageable) {
        String sql = "select lp.*\n" +
                "from author_layer_paper lp\n" +
                "join scinapse.paper p on lp.paper_id = p.id\n" +
                "where lp.author_id = :authorId";

        if (!showAll) {
            sql += "\nand lp.status <> 'PENDING_REMOVE'";
        }

        if (keywords != null && keywords.length > 0) {
            sql += "\nand to_tsvector(p.title) @@ to_tsquery(:keywords)";
        }

        if (sort == null) {
            sort = PaperSort.MOST_CITATIONS;
        }
        switch (sort) {
            case NEWEST_FIRST:
                sql += "\norder by p.year desc";
                break;
            case OLDEST_FIRST:
                sql += "\norder by p.year asc";
                break;
            case RECENTLY_UPDATED:
                sql += "\norder by lp.updated_at desc";
                break;
            default:
                sql += "\norder by p.citation_count desc";
                break;
        }

        sql += "\noffset " + pageable.getOffset();
        sql += "\nlimit " + pageable.getPageSize();

        Query query = getEntityManager()
                .createNativeQuery(sql, AuthorLayerPaper.class)
                .setParameter("authorId", authorId);

        if (keywords != null && keywords.length > 0) {
            String keywordStr = StringUtils.join(keywords, " & ");
            query.setParameter("keywords", keywordStr);
        }

        return query.getResultList();
    }

}