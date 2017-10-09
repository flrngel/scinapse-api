package network.pluto.absolute.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import network.pluto.absolute.security.SkipPathRequestMatcher;
import network.pluto.absolute.security.TokenHelper;
import network.pluto.absolute.security.jwt.JwtAuthenticationProcessingFilter;
import network.pluto.absolute.security.jwt.JwtAuthenticationProvider;
import network.pluto.absolute.security.jwt.JwtAuthenticationSuccessHandler;
import network.pluto.absolute.security.rest.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String AUTH_LOGIN_URL = "/auth/login";
    private static final String AUTH_LOGOUT_URL = "/auth/logout";

    @Value("${jwt.cookie}")
    private String cookie;

    private final LogoutSuccessHandler logoutHandler;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAuthenticationSuccessHandler restSuccessHandler;
    private final RestAuthenticationFailureHandler restFailureHandler;
    private final RestAuthenticationProvider restAuthenticationProvider;
    private final JwtAuthenticationSuccessHandler jwtSuccessHandler;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final ObjectMapper objectMapper;
    private final TokenHelper tokenHelper;

    @Autowired
    public SecurityConfig(LogoutSuccessHandler logoutHandler, RestAuthenticationEntryPoint restAuthenticationEntryPoint, RestAuthenticationSuccessHandler restSuccessHandler, RestAuthenticationFailureHandler restFailureHandler, RestAuthenticationProvider restAuthenticationProvider, JwtAuthenticationSuccessHandler jwtSuccessHandler, JwtAuthenticationProvider jwtAuthenticationProvider, ObjectMapper objectMapper, TokenHelper tokenHelper) {
        this.logoutHandler = logoutHandler;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.restSuccessHandler = restSuccessHandler;
        this.restFailureHandler = restFailureHandler;
        this.restAuthenticationProvider = restAuthenticationProvider;
        this.jwtSuccessHandler = jwtSuccessHandler;
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
        this.objectMapper = objectMapper;
        this.tokenHelper = tokenHelper;
    }


    private RestAuthenticationProcessingFilter buildProcessingFilter() throws Exception {
        AntPathRequestMatcher matcher = new AntPathRequestMatcher(AUTH_LOGIN_URL, HttpMethod.POST.name());
        RestAuthenticationProcessingFilter filter = new RestAuthenticationProcessingFilter(matcher, objectMapper);
        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setAuthenticationSuccessHandler(restSuccessHandler);
        filter.setAuthenticationFailureHandler(restFailureHandler);
        return filter;
    }

    private JwtAuthenticationProcessingFilter buildJwtProcessingFilter() throws Exception {
        List<String> skipPaths = Arrays.asList(AUTH_LOGIN_URL, AUTH_LOGOUT_URL);
        SkipPathRequestMatcher matcher = new SkipPathRequestMatcher(skipPaths, "/**");
        JwtAuthenticationProcessingFilter filter = new JwtAuthenticationProcessingFilter(matcher, tokenHelper);
        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setAuthenticationSuccessHandler(jwtSuccessHandler);
        filter.setAuthenticationFailureHandler(restFailureHandler);
        return filter;
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

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(restAuthenticationProvider);
        auth.authenticationProvider(jwtAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .headers()
                .frameOptions().sameOrigin();

        http
                .csrf().disable()
                .cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint);

        http
                .addFilterBefore(buildProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(buildJwtProcessingFilter(), UsernamePasswordAuthenticationFilter.class);

        http
                .authorizeRequests()
                .antMatchers(AUTH_LOGIN_URL, AUTH_LOGOUT_URL).permitAll()
                .anyRequest().authenticated();

        http
                .logout()
                .logoutUrl(AUTH_LOGOUT_URL)
                .logoutSuccessHandler(logoutHandler)
                .deleteCookies(cookie);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers(
                        HttpMethod.GET,
                        "/",
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
                )
                .antMatchers(
                        HttpMethod.GET,
                        "/hello",
                        "/articles",
                        "/articles/*"
                )
                .antMatchers(
                        HttpMethod.POST,
                        "/members",
                        "/members/checkDuplication"
                );
    }
}
