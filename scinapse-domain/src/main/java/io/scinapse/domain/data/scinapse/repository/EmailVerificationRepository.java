package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.data.scinapse.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    EmailVerification findByToken(String token);
}
