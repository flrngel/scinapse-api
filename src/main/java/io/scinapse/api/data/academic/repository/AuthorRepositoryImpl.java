package io.scinapse.api.data.academic.repository;

import io.scinapse.api.data.academic.Author;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class AuthorRepositoryImpl extends QueryDslRepositorySupport implements AuthorRepositoryCustom {

    public AuthorRepositoryImpl() {
        super(Author.class);
    }

    @PersistenceContext(unitName = "academic")
    @Override
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }

}
