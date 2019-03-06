package io.scinapse.domain.data.academic.repository;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.domain.data.academic.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@XRayEnabled
public interface AuthorRepository extends JpaRepository<Author, Long>, AuthorRepositoryCustom {
    List<Author> findByIdIn(List<Long> authorIds);
}
