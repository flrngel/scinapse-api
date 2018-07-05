package network.pluto.absolute.repository.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import network.pluto.absolute.model.mag.FieldsOfStudy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@XRayEnabled
public interface FieldsOfStudyRepository extends JpaRepository<FieldsOfStudy, Long> {
    List<FieldsOfStudy> findByIdIn(List<Long> fosIds);
}
