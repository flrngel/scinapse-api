package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.data.scinapse.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
}
