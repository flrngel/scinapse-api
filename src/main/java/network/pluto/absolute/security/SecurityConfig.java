package network.pluto.absolute.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final TokenHelper tokenHelper;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final RestLogoutSuccessHandler restLogoutSuccessHandler;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService,
                          TokenHelper tokenHelper,
                          AuthenticationSuccessHandler authenticationSuccessHandler,
                          RestLogoutSuccessHandler restLogoutSuccessHandler,
                          PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.tokenHelper = tokenHelper;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.restLogoutSuccessHandler = restLogoutSuccessHandler;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${jwt.cookie}")
    private String cookie;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().frameOptions().disable();

        http
                .csrf().disable()
                .cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .addFilterBefore(new TokenAuthenticationFilter(tokenHelper, userDetailsService), BasicAuthenticationFilter.class);

        http
                .authorizeRequests()
                .antMatchers("/auth/refresh").hasAnyRole("USER", "ADMIN")
                .anyRequest().permitAll();

        http
                .logout()
                .logoutUrl("/auth/logout")
                .logoutSuccessHandler(restLogoutSuccessHandler)
                .deleteCookies(cookie);

//        http
//                .formLogin()
//                .loginPage("/auth/token")
//                .successHandler(authenticationSuccessHandler);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                HttpMethod.GET,
                "/",
                "/*.html",
                "/favicon.ico",
                "/**/*.html",
                "/**/*.css",
                "/**/*.js"
        );
    }
}
