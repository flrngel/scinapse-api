package io.scinapse.domain.data.scinapse.repository.author;

import io.scinapse.domain.data.scinapse.model.author.AuthorLayerFos;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class AuthorLayerFosRepositoryImpl extends QueryDslRepositorySupport implements AuthorLayerFosRepositoryCustom {

    public AuthorLayerFosRepositoryImpl() {
        super(AuthorLayerFos.class);
    }

}
