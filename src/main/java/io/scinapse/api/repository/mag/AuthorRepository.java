package io.scinapse.api.repository.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.model.mag.Author;
import org.springframework.data.jpa.repository.JpaRepository;

@XRayEnabled
public interface AuthorRepository extends JpaRepository<Author, Long>, AuthorRepositoryCustom {
}
