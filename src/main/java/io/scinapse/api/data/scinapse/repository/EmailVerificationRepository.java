package io.scinapse.api.data.scinapse.repository;

import io.scinapse.api.data.scinapse.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    EmailVerification findByToken(String token);
}
