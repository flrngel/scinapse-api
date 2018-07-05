package network.pluto.absolute.repositories;

import network.pluto.absolute.enums.AuthorityName;
import network.pluto.absolute.models.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Authority findByName(AuthorityName name);
}
