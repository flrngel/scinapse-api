package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.data.scinapse.model.Member;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;

public class MemberRepositoryImpl extends QueryDslRepositorySupport implements MemberRepositoryCustom {

    public MemberRepositoryImpl() {
        super(Member.class);
    }

    @Override
    public List<Member> findInDate(Date targetDate) {
        String sql = "select * from scinapse.member where cast(created_at as date) = cast(:targetDate as date)";

        return getEntityManager()
                .createNativeQuery(sql, Member.class)
                .setParameter("targetDate", targetDate, TemporalType.DATE)
                .getResultList();
    }

}
