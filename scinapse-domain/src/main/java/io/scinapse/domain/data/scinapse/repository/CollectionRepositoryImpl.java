package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.data.scinapse.model.Collection;
import io.scinapse.domain.dto.CollectionWrapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CollectionRepositoryImpl extends QueryDslRepositorySupport implements CollectionRepositoryCustom {

    public CollectionRepositoryImpl() {
        super(Collection.class);
    }

    @PersistenceContext(unitName = "scinapse")
    @Override
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }

    @Override
    public Map<Long, List<CollectionWrapper>> findBySavedPapers(long memberId, Set<Long> paperIds) {
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
                            CollectionWrapper wrapper = new CollectionWrapper();
                            wrapper.setId(((BigInteger) obj[1]).longValue());
                            wrapper.setTitle((String) obj[2]);
                            return wrapper;
                        }, Collectors.toList())
                ));
    }

    @Override
    public List<CollectionEmailDataWrapper> getCollectionEmailData() {
        String sql = "select t.member_id, m.email, m.first_name, m.last_name, t.collection_id, t.collection_title, t.paper_id, t.title, t.year, t.count\n" +
                "from (\n" +
                "       select\n" +
                "         rp.collection_id,\n" +
                "         c.title as collection_title,\n" +
                "         rp.paper_id,\n" +
                "         rp.title,\n" +
                "         rp.year,\n" +
                "         c.member_id,\n" +
                "         row_number()\n" +
                "         over (\n" +
                "           partition by member_id\n" +
                "           order by rp.created_at desc ) as row,\n" +
                "         count(1)\n" +
                "         over (\n" +
                "           partition by member_id )      as count\n" +
                "       from rel_collection_paper rp\n" +
                "         join collection c on rp.collection_id = c.id\n" +
                "       where cast(rp.created_at as date) = date 'yesterday'\n" +
                "     ) t\n" +
                "  join member m on t.member_id = m.id\n" +
                "where t.row < 4 and (m.email_verified = true or m.password is null)";


        Query query = getEntityManager()
                .createNativeQuery(sql);

        List<Object[]> list = query.getResultList();
        return list.stream()
                .map(obj -> {
                    CollectionEmailDataWrapper wrapper = new CollectionEmailDataWrapper();

                    wrapper.setMemberId(((BigInteger) obj[0]).longValue());
                    wrapper.setEmail((String) obj[1]);
                    wrapper.setFirstName((String) obj[2]);
                    wrapper.setLastName((String) obj[3]);

                    wrapper.setCollectionId(((BigInteger) obj[4]).longValue());
                    wrapper.setCollectionTitle((String) obj[5]);

                    wrapper.setPaperId(((BigInteger) obj[6]).longValue());
                    wrapper.setPaperTitle((String) obj[7]);
                    wrapper.setPaperYear((Integer) obj[8]);

                    wrapper.setCount(((BigInteger) obj[9]).intValue());
                    return wrapper;

                })
                .collect(Collectors.toList());
    }

    @Getter
    @Setter
    public class CollectionEmailDataWrapper {
        private long memberId;
        private String email;
        private String firstName;
        private String lastName;
        private long collectionId;
        private String collectionTitle;
        private long paperId;
        private String paperTitle;
        private Integer paperYear;
        private int count;
    }

}
