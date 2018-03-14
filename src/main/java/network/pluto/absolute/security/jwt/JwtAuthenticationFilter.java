package network.pluto.absolute.security.jwt;

import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import network.pluto.absolute.security.TokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenHelper tokenHelper;

    @Autowired
    public JwtAuthenticationFilter(TokenHelper tokenHelper) {
        this.tokenHelper = tokenHelper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {

            // get token
            String jwt = tokenHelper.getToken(request);

            if (!Strings.isNullOrEmpty(jwt)) {

                // refresh token
                String refreshedToken = tokenHelper.refreshToken(jwt);
                tokenHelper.addCookie(response, refreshedToken);

                // get claims
                Claims claims = tokenHelper.getAllClaimsFromToken(refreshedToken);
                Integer memberId = claims.get("id", Integer.class);
                String name = claims.get("name", String.class);
                Boolean oauthLogin = claims.get("oauth", Boolean.class);
                List<String> roles = claims.get("roles", List.class);
                List<GrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

                // set principal
                JwtUser jwtUser = new JwtUser(authorities);
                jwtUser.setId(memberId);
                jwtUser.setEmail(claims.getSubject());
                jwtUser.setName(name);
                jwtUser.setOauthLogin(oauthLogin);
                jwtUser.setToken(refreshedToken);

                // set authentication
                SecurityContextHolder.getContext().setAuthentication(jwtUser);
            }

        } catch (Exception e) {

            // remove jwt cookie
            tokenHelper.removeCookie(response);
        }

        filterChain.doFilter(request, response);
    }

}
