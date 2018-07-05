package io.scinapse.api.repository;

import io.scinapse.api.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    EmailVerification findByToken(String token);
}
