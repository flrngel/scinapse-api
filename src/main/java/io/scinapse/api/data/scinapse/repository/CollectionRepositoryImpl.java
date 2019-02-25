package io.scinapse.api.data.scinapse.repository;

import io.scinapse.api.dto.v2.PaperItemDto;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CollectionRepositoryImpl extends QueryDslRepositorySupport implements CollectionRepositoryCustom {

    public CollectionRepositoryImpl() {
        super(io.scinapse.api.data.scinapse.model.Collection.class);
    }

    @PersistenceContext(unitName = "scinapse")
    @Override
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }

    @Override
    public Map<Long, List<PaperItemDto.SavedInCollection>> findBySavedPapers(long memberId, Set<Long> paperIds) {
        String sql = "select t.paper_id, t.id, t.title\n" +
                "from (\n" +
                "       select cp.paper_id, c.id, c.title,\n" +
                "         ROW_NUMBER() over (partition by cp.paper_id order by cp.created_at desc ) as row\n" +
                "       from collection c\n" +
                "         join rel_collection_paper cp on c.id = cp.collection_id\n" +
                "       where member_id = :memberId and cp.paper_id in (:paperIds)\n" +
                "     ) t\n" +
                "where t.row < 3";

        Query query = getEntityManager()
                .createNativeQuery(sql)
                .setParameter("memberId", memberId)
                .setParameter("paperIds", paperIds);

        List<Object[]> list = query.getResultList();
        return list.stream()
                .collect(Collectors.groupingBy(obj -> ((BigInteger) obj[0]).longValue(),
                        Collectors.mapping(obj -> {
                            PaperItemDto.SavedInCollection additional = new PaperItemDto.SavedInCollection();
                            additional.setId(((BigInteger) obj[1]).longValue());
                            additional.setTitle((String) obj[2]);
                            return additional;
                        }, Collectors.toList())
                ));
    }

}
