package io.scinapse.domain.data.academic.repository;

import io.scinapse.domain.data.academic.model.Author;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

public class AuthorRepositoryImpl extends QueryDslRepositorySupport implements AuthorRepositoryCustom {

    public AuthorRepositoryImpl() {
        super(Author.class);
    }

}
