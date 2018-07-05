package network.pluto.absolute.repositories;

import network.pluto.absolute.models.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    EmailVerification findByToken(String token);
}
