package network.pluto.absolute.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import network.pluto.bibliotheca.enums.AuthorityName;
import network.pluto.bibliotheca.models.Authority;
import network.pluto.bibliotheca.models.Member;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.Cookie;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class TokenHelperTest {

    private TokenHelper helper;

    private Member member;

    @Before
    public void setUp() throws Exception {
        this.helper = new TokenHelper();
        ReflectionTestUtils.setField(this.helper, "issuer", "pluto");
        ReflectionTestUtils.setField(this.helper, "secret", "test");
        ReflectionTestUtils.setField(this.helper, "cookie", "pluto_jwt");
        ReflectionTestUtils.setField(this.helper, "expiresIn", 3600);

        this.member = new Member();
        this.member.setId(1);
        this.member.setEmail("alice@pluto.network");
        this.member.setName("alice");

        Authority authority = new Authority();
        authority.setName(AuthorityName.ROLE_USER);
        this.member.setAuthorities(Collections.singletonList(authority));
    }

    @Test
    public void generateToken() throws Exception {
        String token = helper.generateToken(member, true);
        System.out.println(token);

        assertThat(token).isNotEmpty();
    }

    @Test
    public void getAllClaimsFromToken() throws Exception {
        String token = helper.generateToken(member, true);

        Claims claims = Jwts.parser()
                .setSigningKey("test")
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getIssuer()).isEqualTo("pluto");
        assertThat(claims.getSubject()).isEqualTo(member.getEmail());
        assertThat(claims.get("name")).isEqualTo(member.getName());
        assertThat(claims.get("oauth")).isEqualTo(true);
        assertThat(claims.get("roles")).asList().contains("ROLE_USER");
    }

    @Test
    public void refreshToken() throws Exception {
        DefaultClaims claims = new DefaultClaims();
        claims.put("name", "alice");

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuer("pluto")
                .setSubject(member.getEmail())
                .signWith(SignatureAlgorithm.HS512, "test")
                .compact();

        String refreshToken = helper.refreshToken(token);

        Claims refreshed = Jwts.parser()
                .setSigningKey("test")
                .parseClaimsJws(refreshToken)
                .getBody();

        assertThat(refreshToken).isNotEmpty();
        assertThat(refreshed.getIssuer()).isEqualTo("pluto");
        assertThat(refreshed.getSubject()).isEqualTo("alice@pluto.network");
        assertThat(refreshed.get("name")).isEqualTo("alice");
    }

    @Test
    public void getToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie authCookie = new Cookie("pluto_jwt", "token");
        authCookie.setPath("/");
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(3600);
        request.setCookies(authCookie);

        String token = helper.getToken(request);

        assertThat(token).isNotEmpty();
    }

    @Test
    public void addCookie() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        helper.addCookie(response, "token");

        assertThat(response.getCookie("pluto_jwt")).isNotNull();
        assertThat(response.getCookie("pluto_jwt").getValue()).isEqualTo("token");
    }

    @Test
    public void removeCookie() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        Cookie authCookie = new Cookie("pluto_jwt", "token");
        authCookie.setPath("/");
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(3600);
        response.addCookie(authCookie);

        helper.removeCookie(response);

        // FIXME response.addCookie() does not remove previous cookie with same name.
        // It seems that later one overrides previous one from browser.
        // helper.removeCookie() is working now, but it should be refactor later.
        assertThat(response.getCookies()).hasSize(2);
        assertThat(response.getCookies()[1]).isNotNull();
        assertThat(response.getCookies()[1].getValue()).isNull();
        assertThat(response.getCookies()[1].getMaxAge()).isEqualTo(0);
    }

}