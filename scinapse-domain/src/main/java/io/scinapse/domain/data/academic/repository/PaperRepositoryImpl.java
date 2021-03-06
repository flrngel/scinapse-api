package io.scinapse.domain.data.academic.repository;

import io.scinapse.domain.data.academic.model.Paper;
import io.scinapse.domain.data.academic.model.QPaperFieldsOfStudy;
import io.scinapse.domain.data.academic.model.QPaperTopAuthor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import javax.persistence.Query;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PaperRepositoryImpl extends QueryDslRepositorySupport implements PaperRepositoryCustom {

    public PaperRepositoryImpl() {
        super(Paper.class);
    }

    @Override
    public List<Long> calculateFos(Set<Long> paperIds) {
        QPaperFieldsOfStudy paperFos = QPaperFieldsOfStudy.paperFieldsOfStudy;

        return from(paperFos)
                .where(paperFos.id.paperId.in(paperIds))
                .groupBy(paperFos.id.fosId)
                .orderBy(paperFos.id.fosId.count().desc())
                .limit(5)
                .select(paperFos.id.fosId)
                .fetch();
    }

    @Override
    public List<Long> calculateCoauthor(long authorId, Set<Long> paperIds) {
        QPaperTopAuthor topAuthor = QPaperTopAuthor.paperTopAuthor;

        return from(topAuthor)
                .where(topAuthor.id.paperId.in(paperIds)
                        .and(topAuthor.id.authorId.ne(authorId)))
                .groupBy(topAuthor.id.authorId)
                .orderBy(topAuthor.id.authorId.count().desc())
                .limit(5)
                .select(topAuthor.id.authorId)
                .fetch();
    }

    @Override
    public List<Long> extractTopRefPapers(Set<Long> paperIds, Pageable pageable) {
        String sql = "select pr.paper_reference_id\n" +
                "from paper_reference pr\n" +
                "  join paper p on pr.paper_reference_id = p.id\n" +
                "where pr.paper_id in :paperIds\n" +
                "      and (p.doc_type is null\n" +
                "           or p.doc_type in ('Conference', 'Dataset', 'Journal'))\n" +
                "group by pr.paper_reference_id\n" +
                "having count(*) > 2\n" +
                "order by count(*) desc, max(p.year) desc";

        Query query = getEntityManager()
                .createNativeQuery(sql)
                .setParameter("paperIds", paperIds);

        List<Object> list = query
                .setFirstResult(pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return list.stream()
                .map(obj -> ((BigInteger) obj).longValue())
                .collect(Collectors.toList());
    }

}
