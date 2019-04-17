package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.data.scinapse.model.Collection;
import io.scinapse.domain.dto.CollectionWrapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class CollectionRepositoryImpl extends QueryDslRepositorySupport implements CollectionRepositoryCustom {

    public CollectionRepositoryImpl() {
        super(Collection.class);
    }

    @Override
    public Map<Long, List<CollectionWrapper>> findBySavedPapers(long memberId, Set<Long> paperIds) {
        String sql = "select\n" +
                "  cp.paper_id,\n" +
                "  c.id,\n" +
                "  c.title,\n" +
                "  c.updated_at\n" +
                "from scinapse.collection c\n" +
                "  join scinapse.rel_collection_paper cp on c.id = cp.collection_id\n" +
                "where member_id = :memberId and cp.paper_id in :paperIds";

        Query query = getEntityManager()
                .createNativeQuery(sql)
                .setParameter("memberId", memberId)
                .setParameter("paperIds", paperIds);

        List<Object[]> list = query.getResultList();

        Map<Long, List<CollectionWrapper>> collectionMap = list
                .stream()
                .collect(Collectors.groupingBy(obj -> ((BigInteger) obj[0]).longValue(),
                        Collectors.mapping(obj -> {
                            CollectionWrapper wrapper = new CollectionWrapper();
                            wrapper.setId(((BigInteger) obj[1]).longValue());
                            wrapper.setTitle((String) obj[2]);
                            wrapper.setUpdatedAt(OffsetDateTime.ofInstant(((Timestamp) obj[3]).toInstant(), ZoneOffset.systemDefault()));
                            return wrapper;
                        }, Collectors.toList())
                ));
        collectionMap.values().forEach(v -> v.sort(Comparator.comparing(CollectionWrapper::getUpdatedAt).reversed()));

        return collectionMap;
    }

    @Override
    public List<CollectionEmailDataWrapper> getCollectionEmailData(Date targetDate) {
        String sql = "select t.member_id, m.email, m.first_name, m.last_name, t.collection_id, t.collection_title, t.paper_id, t.title, t.year, t.count, t.start\n" +
                "from (\n" +
                "       select\n" +
                "         rp.collection_id,\n" +
                "         c.title as collection_title,\n" +
                "         rp.paper_id,\n" +
                "         rp.title,\n" +
                "         rp.year,\n" +
                "         c.member_id,\n" +
                "         row_number()\n" +
                "         over ( partition by member_id, collection_id order by rp.created_at desc ) as row,\n" +
                "         count(1) over ( partition by member_id )      as count,\n" +
                "         min(rp.created_at) over (partition by member_id) as start\n" +
                "       from scinapse.rel_collection_paper rp\n" +
                "         join scinapse.collection c on rp.collection_id = c.id\n" +
                "       where cast(rp.created_at as date) = cast(:targetDate as date)\n" +
                "     ) t\n" +
                "  join scinapse.member m on t.member_id = m.id\n" +
                "where t.row < 4 and (m.email_verified = 1 or m.password is null)";

        Query query = getEntityManager()
                .createNativeQuery(sql)
                .setParameter("targetDate", targetDate, TemporalType.DATE);

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

                    wrapper.setCount((int) obj[9]);
                    wrapper.setStart(OffsetDateTime.ofInstant(((Timestamp) obj[10]).toInstant(), ZoneOffset.systemDefault()));
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
        private OffsetDateTime start;
    }

}
