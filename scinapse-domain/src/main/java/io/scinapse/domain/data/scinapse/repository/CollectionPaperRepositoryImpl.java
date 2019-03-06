package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.data.scinapse.model.CollectionPaper;
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

public class CollectionPaperRepositoryImpl extends QueryDslRepositorySupport implements CollectionPaperRepositoryCustom {

    public CollectionPaperRepositoryImpl() {
        super(CollectionPaper.class);
    }

    @PersistenceContext(unitName = "scinapse")
    @Override
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }

    @Override
    public Page<CollectionPaper> findPapers(long collectionId, String[] keywords, PaperSort sort, Pageable pageable) {
        String sql = "select cp.*\n" +
                "from rel_collection_paper cp\n" +
                "where cp.collection_id = :collectionId";
        String count = "select count(1) from rel_collection_paper cp where cp.collection_id = :collectionId";

        if (keywords != null && keywords.length > 0) {
            sql += "\nand to_tsvector(cp.title) @@ to_tsquery(:keywords)";
            count += "\nand to_tsvector(cp.title) @@ to_tsquery(:keywords)";
        }

        if (sort == null) {
            sort = PaperSort.RECENTLY_ADDED;
        }
        switch (sort) {
            case MOST_CITATIONS:
                sql += "\norder by cp.citation_count desc nulls last";
                break;
            case NEWEST_FIRST:
                sql += "\norder by cp.year desc nulls last";
                break;
            case OLDEST_FIRST:
                sql += "\norder by cp.year asc nulls last";
                break;
            default:
                sql += "\norder by cp.updated_at desc nulls last";
                break;
        }

        Query countQuery = getEntityManager()
                .createNativeQuery(count)
                .setParameter("collectionId", collectionId);

        Query query = getEntityManager()
                .createNativeQuery(sql, CollectionPaper.class)
                .setParameter("collectionId", collectionId);

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
