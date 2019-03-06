package io.scinapse.api.util;

import io.scinapse.api.error.IdGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

@Slf4j
public class IdUtils {

    private static final SecureRandom RANDOM_GENERATOR = new SecureRandom();

    public static String generateStringId(JpaRepository<?, String> repository) {
        return new StringIdGenerator(repository).findAvailableId();
    }

    public static class StringIdGenerator {
        private JpaRepository<?, String> repository;
        private String generatedId;

        public StringIdGenerator(JpaRepository<?, String> repository) {
            Objects.requireNonNull(repository, "repository must not be null");
            this.repository = repository;
            this.generatedId = generateId();
        }

        private String generateId() {
            byte[] randomBytes = new byte[15];
            RANDOM_GENERATOR.nextBytes(randomBytes);

            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
            if (StringUtils.isBlank(encoded) || (encoded.length() != 20)) {
                throw new IdGenerationException("Generated String ID is invalid: " + encoded);
            }

            return encoded;
        }

        /**
         * Generate String ID for table identifier.
         * <p>
         * 1. generate 15 bytes with random value
         * - [102, -124, -52, 87, -62, 25, -79, 111, 5, 50, 46, 106, -33, -103, -78]
         * 2. encode byte array to String using base64 url encoder
         * - "ZoTMV8IZsW8FMi5q35my"
         * 3. slice encoded ID & find available
         * - "ZoTMV8IZ"
         * - "ZoTMV8IZsW"
         * - "ZoTMV8IZsW8F"
         * - "ZoTMV8IZsW8FMi"
         * - "ZoTMV8IZsW8FMi5q"
         * - "ZoTMV8IZsW8FMi5q35"
         * - "ZoTMV8IZsW8FMi5q35my"
         * 4. throw an exception if all candidates collide.
         *
         * @return generated random String ID.
         */
        public String findAvailableId() {
            for (int end = 8; end <= 20; end += 2) {
                String nextId = generatedId.substring(0, end);
                if (repository.exists(nextId)) {
                    log.warn("ID collision occurs. ID: {}, repository: {}", generatedId, getRepositoryName());
                    continue;
                }
                return nextId;
            }
            throw new IdGenerationException("ID collision occurs. ID: " + generatedId + ", repository: " + getRepositoryName());
        }

        private String getRepositoryName() {
            Class<?>[] interfaces = repository.getClass().getInterfaces();
            if (interfaces.length == 0) {
                log.warn("cannot get repository name: {}", repository.getClass().getName());
                return repository.getClass().getName();
            }
            return interfaces[0].getSimpleName();
        }
    }

}
