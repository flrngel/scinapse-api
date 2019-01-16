package io.scinapse.api.data.academic.repository;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.data.academic.Paper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@XRayEnabled
@Repository
public interface PaperRepository extends JpaRepository<Paper, Long>, PaperRepositoryCustom {

    List<Paper> findByIdIn(List<Long> paperIds);
    List<Paper> findByIdIn(Set<Long> paperIds);

    @Query("select p.id, p.title, p.citationCount from Paper p where p.id in :paperIds order by p.citationCount desc")
    List<Object[]> findAllPaperTitle(@Param("paperIds") Set<Long> paperIds);

}
