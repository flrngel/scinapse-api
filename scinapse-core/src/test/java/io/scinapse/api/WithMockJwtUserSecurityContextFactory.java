package io.scinapse.api;

import io.scinapse.api.security.jwt.JwtUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WithMockJwtUserSecurityContextFactory implements WithSecurityContextFactory<WithMockJwtUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockJwtUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // get authorities
        List<GrantedAuthority> authorities = Arrays.stream(customUser.roles()).map(name -> new SimpleGrantedAuthority(name.name())).collect(Collectors.toList());

        // set principal
        JwtUser jwtUser = new JwtUser(authorities);
        jwtUser.setId(customUser.memberId());
        jwtUser.setEmail(customUser.email());
        jwtUser.setName(customUser.firstName() + " " + customUser.lastName());
        jwtUser.setOauthLogin(customUser.oauth());
        jwtUser.setToken("token");

        context.setAuthentication(jwtUser);
        return context;
    }

}
