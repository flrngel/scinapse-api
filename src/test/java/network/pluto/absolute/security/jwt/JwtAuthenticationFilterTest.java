package network.pluto.absolute.security.jwt;

import io.jsonwebtoken.impl.DefaultClaims;
import network.pluto.absolute.enums.AuthorityName;
import network.pluto.absolute.security.TokenExpiredException;
import network.pluto.absolute.security.TokenHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;

    @MockBean
    private MockHttpServletRequest request;

    @MockBean
    private MockHttpServletResponse response;

    @MockBean
    private MockFilterChain chain;

    @MockBean
    private TokenHelper tokenHelper;

    @Before
    public void setUp() throws Exception {
        this.filter = new JwtAuthenticationFilter(this.tokenHelper);
    }

    @Test
    public void doFilterInternal() throws Exception {
        String token = "token";
        String refreshToken = "refresh";

        DefaultClaims claims = new DefaultClaims();
        claims.setIssuer("pluto");
        claims.setSubject("alice@pluto.network");
        claims.put("id", 1);
        claims.put("name", "alice");
        claims.put("oauth", true);
        claims.put("roles", Collections.singletonList(AuthorityName.ROLE_USER.name()));

        when(tokenHelper.getToken(request)).thenReturn(token);
        when(tokenHelper.refreshToken(token)).thenReturn(refreshToken);
        when(tokenHelper.getAllClaimsFromToken(refreshToken)).thenReturn(claims);

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(tokenHelper).getToken(request);
        verify(tokenHelper).refreshToken(token);
        verify(tokenHelper).getAllClaimsFromToken(refreshToken);
        verify(tokenHelper, never()).removeCookie(any());
        verify(chain).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_without_jwt_token() throws Exception {
        when(tokenHelper.getToken(request)).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenHelper).getToken(request);
        verifyNoMoreInteractions(tokenHelper);
        verify(chain).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_with_invalid_token() throws Exception {
        String token = "token";

        when(tokenHelper.getToken(request)).thenReturn(token);
        when(tokenHelper.refreshToken(token)).thenThrow(new TokenExpiredException("JWT Token expired", token, "expired"));

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenHelper).getToken(request);
        verify(tokenHelper).refreshToken(token);
        verify(tokenHelper).removeCookie(response);
        verifyNoMoreInteractions(tokenHelper);
        verify(chain).doFilter(request, response);
    }

}