package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.data.scinapse.model.Member;

import java.util.Date;
import java.util.List;

public interface MemberRepositoryCustom {
    List<Member> findInDate(Date date);
}
