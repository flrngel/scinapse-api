package io.scinapse.api.repository.mag;

import io.scinapse.api.model.mag.Author;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

public class AuthorRepositoryImpl extends QueryDslRepositorySupport implements AuthorRepositoryCustom {

    public AuthorRepositoryImpl() {
        super(Author.class);
    }

}
