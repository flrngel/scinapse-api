package network.pluto.absolute.repositories.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import network.pluto.absolute.models.mag.Paper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@XRayEnabled
@Repository
public interface PaperRepository extends JpaRepository<Paper, Long> {
    List<Paper> findByIdIn(List<Long> paperIds);
}
