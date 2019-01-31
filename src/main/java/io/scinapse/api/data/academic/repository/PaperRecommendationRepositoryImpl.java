package io.scinapse.api.data.academic.repository;

import io.scinapse.api.data.academic.PaperRecommendation;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PaperRecommendationRepositoryImpl extends QueryDslRepositorySupport implements PaperRecommendationRepositoryCustom {

    public PaperRecommendationRepositoryImpl() {
        super(PaperRecommendation.class);
    }

    @PersistenceContext(unitName = "academic")
    @Override
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }

    @Override
    public List<Long> getReadingNow(long paperId) {
        String sql = "select distinct (recommended_paper_id)\n" +
                "from paper_recommendation\n" +
                "where paper_id in (select :paperId\n" +
                "                   union\n" +
                "                   select recommended_paper_id\n" +
                "                   from paper_recommendation\n" +
                "                   where paper_id = :paperId)\n" +
                "      and recommended_paper_id != :paperId";

        List list = getEntityManager()
                .createNativeQuery(sql)
                .setParameter("paperId", paperId)
                .getResultList();

        Collections.shuffle(list);

        return (List<Long>) list
                .stream()
                .limit(10)
                .map(el -> ((BigInteger) el).longValue())
                .collect(Collectors.toList());
    }

}
