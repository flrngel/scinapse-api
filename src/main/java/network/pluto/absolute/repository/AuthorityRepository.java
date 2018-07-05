package network.pluto.absolute.repository;

import network.pluto.absolute.enums.AuthorityName;
import network.pluto.absolute.model.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Authority findByName(AuthorityName name);
}
