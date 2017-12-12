package network.pluto.absolute.configuration;

import network.pluto.absolute.security.jwt.JwtAuthenticationFilter;
import network.pluto.absolute.security.rest.RestAccessDeniedHandler;
import network.pluto.absolute.security.rest.RestAuthExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
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
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String AUTH_LOGIN_URI = "/auth/login";
    private static final String AUTH_LOGOUT_URI = "/auth/logout";
    private static final String AUTH_OAUTH_AUTHORIZE_URI = "/auth/oauth/authorize-uri";
    private static final String AUTH_OAUTH_EXCHANGE_URI = "/auth/oauth/exchange";
    private static final String AUTH_OAUTH_LOGIN_URI = "/auth/oauth/login";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthExceptionHandler restAuthExceptionHandler;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          RestAuthExceptionHandler restAuthExceptionHandler,
                          RestAccessDeniedHandler restAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.restAuthExceptionHandler = restAuthExceptionHandler;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
    }

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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .headers()
                .frameOptions().sameOrigin();

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

                // permit get
                .antMatchers(
                        HttpMethod.GET,
                        "/verification",
                        "/members/checkDuplication",
                        "/members/*",
                        "/members/*/articles",
                        "/members/*/reviews",
                        "/articles",
                        "/articles/*",
                        "/articles/*/point",
                        "/articles/*/reviews",
                        "/articles/*/reviews/*",
                        "/articles/*/reviews/*/vote",
                        "/articles/*/reviews/*/comments",
                        "/papers",
                        "/papers/*",
                        "/papers/*/references",
                        "/papers/*/cited",
                        "/papers/search"
                ).permitAll()

                // permit post
                .antMatchers(
                        HttpMethod.POST,
                        "/members"
                ).permitAll()

                .anyRequest()
                .authenticated();
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
