package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.data.scinapse.model.CollectionPaper;
import io.scinapse.domain.enums.PaperSort;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import javax.persistence.Query;
import java.util.List;

public class CollectionPaperRepositoryImpl extends QueryDslRepositorySupport implements CollectionPaperRepositoryCustom {

    public CollectionPaperRepositoryImpl() {
        super(CollectionPaper.class);
    }

    @Override
    public Page<CollectionPaper> findPapers(long collectionId, String[] keywords, PaperSort sort, Pageable pageable) {
        String sql = "select cp.*\n" +
                "from scinapse.rel_collection_paper cp\n" +
                "where cp.collection_id = :collectionId";
        String count = "select count(1) from scinapse.rel_collection_paper cp where cp.collection_id = :collectionId";

        if (keywords != null && keywords.length > 0) {
            sql += "\nand contains(cp.title, :keywords)";
            count += "\nand contains(cp.title, :keywords)";
        }

        if (sort == null) {
            sort = PaperSort.RECENTLY_ADDED;
        }
        switch (sort) {
            case MOST_CITATIONS:
                // desc defaults to nulls last
                sql += "\norder by cp.citation_count desc";
                break;
            case NEWEST_FIRST:
                sql += "\norder by cp.year desc";
                break;
            case OLDEST_FIRST:
                // SQL Server asc nulls last workaround
                sql += "\norder by case when cp.year is null then 2 else 1 end, cp.year asc";
                break;
            default:
                sql += "\norder by cp.updated_at desc";
                break;
        }

        Query countQuery = getEntityManager()
                .createNativeQuery(count)
                .setParameter("collectionId", collectionId);

        Query query = getEntityManager()
                .createNativeQuery(sql, CollectionPaper.class)
                .setParameter("collectionId", collectionId);

        if (keywords != null && keywords.length > 0) {
            String keywordStr = StringUtils.join(keywords, " AND ");
            query.setParameter("keywords", keywordStr);
            countQuery.setParameter("keywords", keywordStr);
        }

        int totalCount = ((int) countQuery.getSingleResult());

        List results = query
                .setFirstResult(pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(results, pageable, totalCount);
    }

}
