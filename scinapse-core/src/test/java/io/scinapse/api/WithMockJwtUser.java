package io.scinapse.api;

import io.scinapse.domain.enums.AuthorityName;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockJwtUserSecurityContextFactory.class)
public @interface WithMockJwtUser {

    long memberId() default 1;

    String firstName() default "alice";

    String lastName() default "bob";

    String email() default "alice@pluto.network";

    boolean oauth() default false;

    AuthorityName[] roles() default { AuthorityName.ROLE_USER };

}
