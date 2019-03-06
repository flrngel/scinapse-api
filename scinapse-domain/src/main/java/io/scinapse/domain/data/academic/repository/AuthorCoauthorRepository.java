package io.scinapse.domain.data.academic.repository;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.domain.data.academic.AuthorCoauthor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@XRayEnabled
public interface AuthorCoauthorRepository extends JpaRepository<AuthorCoauthor, AuthorCoauthor.CoauthorId> {
    List<AuthorCoauthor> findByAuthorId(long authorIds);
}
