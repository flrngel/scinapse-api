package network.pluto.absolute.security;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenHelper tokenHelper;

    public TokenAuthenticationFilter(TokenHelper tokenHelper) {
        this.tokenHelper = tokenHelper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String username = null;
        String authToken = tokenHelper.getToken(request);

        if (authToken != null) {
            try {
                username = tokenHelper.getUsernameFromToken(authToken);
            } catch (IllegalArgumentException | ExpiredJwtException e) {
                log.error("an error occured during getting username from token", e);
            }

            if (username != null) {
//                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//
//                TokenBasedAuthentication authentication = new TokenBasedAuthentication(userDetails);
//                authentication.setToken(authToken);
//                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
