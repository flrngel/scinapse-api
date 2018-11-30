package io.scinapse.api.data.scinapse.repository.author;

import io.scinapse.api.data.scinapse.model.author.AuthorLayerFos;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class AuthorLayerFosRepositoryImpl extends QueryDslRepositorySupport implements AuthorLayerFosRepositoryCustom {

    public AuthorLayerFosRepositoryImpl() {
        super(AuthorLayerFos.class);
    }

    @PersistenceContext(unitName = "scinapse")
    @Override
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }

}
