package io.scinapse.api.data.scinapse.repository;

import io.scinapse.api.data.scinapse.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
}
