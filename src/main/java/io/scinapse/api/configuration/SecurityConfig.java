package io.scinapse.api.configuration;

import io.scinapse.api.enums.AuthorityName;
import io.scinapse.api.security.jwt.JwtAuthenticationFilter;
import io.scinapse.api.security.rest.RestAccessDeniedHandler;
import io.scinapse.api.security.rest.RestAuthExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String AUTH_LOGIN_URI = "/auth/login";
    private static final String AUTH_LOGOUT_URI = "/auth/logout";
    private static final String AUTH_OAUTH_AUTHORIZE_URI = "/auth/oauth/authorize-uri";
    private static final String AUTH_OAUTH_EXCHANGE_URI = "/auth/oauth/exchange";
    private static final String AUTH_OAUTH_LOGIN_URI = "/auth/oauth/login";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthExceptionHandler restAuthExceptionHandler;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors().and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http
                .exceptionHandling()
                .authenticationEntryPoint(restAuthExceptionHandler)
                .accessDeniedHandler(restAccessDeniedHandler);

        http
                .addFilterAfter(jwtAuthenticationFilter, SecurityContextPersistenceFilter.class);

        http
                .logout().disable(); // use custom logout

        http
                .authorizeRequests()

                // permit all http methods
                .antMatchers(
                        AUTH_LOGIN_URI,
                        AUTH_LOGOUT_URI,
                        AUTH_OAUTH_AUTHORIZE_URI,
                        AUTH_OAUTH_EXCHANGE_URI,
                        AUTH_OAUTH_LOGIN_URI
                ).permitAll()

                .antMatchers(
                        HttpMethod.GET,
                        "/members/me/collections"
                )
                .hasAnyAuthority(AuthorityName.ROLE_ADMIN.name(), AuthorityName.ROLE_USER.name())

                // permit get
                .antMatchers(
                        HttpMethod.GET,
                        "/members/checkDuplication",
                        "/members/*",
                        "/members/*/collections",
                        "/papers",
                        "/papers/*",
                        "/papers/*/references",
                        "/papers/*/cited",
                        "/papers/*/related",
                        "/papers/*/authors",
                        "/papers/*/authors/*/related",
                        "/papers/*/citation",
                        "/papers/*/comments",
                        "/papers/search",
                        "/authors/*",
                        "/authors/*/papers",
                        "/authors/*/co-authors",
                        "/journals/*",
                        "/journals/*/papers",
                        "/collections/*",
                        "/collections/*/papers",
                        "/comments",
                        "/complete",
                        "/suggest",
                        "/commitId"
                ).permitAll()

                // permit post
                .antMatchers(
                        HttpMethod.POST,
                        "/email-verification",
                        "/email-verification/resend",
                        "/members",
                        "/members/oauth",
                        "/members/password-token",
                        "/members/reset-password"
                ).permitAll()

                .anyRequest()
                .hasAnyAuthority(AuthorityName.ROLE_ADMIN.name(), AuthorityName.ROLE_USER.name());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers(
                        HttpMethod.GET,
                        "/",
                        "/hello",
                        "/favicon.ico",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js"
                )
                .antMatchers(
                        "/h2-console/**",
                        "/webjars/springfox-swagger-ui/**",
                        "/swagger-resources/**",
                        "/swgr/**"
                );
    }

}
